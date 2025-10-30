from __future__ import annotations

import difflib
from typing import Optional

import filetype
from PyQt6.QtCore import Qt, QRect
from PyQt6.QtGui import QTextCharFormat, QColor, QImage, QPixmap, QPainter
from PyQt6.QtWidgets import (
    QWidget,
    QSplitter,
    QVBoxLayout,
    QPlainTextEdit,
    QLabel,
    QScrollArea,
)


class LineNumberArea(QWidget):
    def __init__(self, editor: "LineNumberEditor", side: str) -> None:
        super().__init__(editor)
        self._editor = editor
        self._side = side  # 'left' or 'right'

    def sizeHint(self):  # noqa: D401
        return self._editor.lineNumberAreaSize()

    def paintEvent(self, event):  # noqa: D401
        self._editor.lineNumberAreaPaintEvent(event, self._side)


class LineNumberEditor(QPlainTextEdit):
    def __init__(self, gutter_side: str) -> None:
        super().__init__()
        self._gutter_side = gutter_side  # 'left' or 'right'
        self._lineNumberArea = LineNumberArea(self, gutter_side)
        self.blockCountChanged.connect(self.updateLineNumberAreaWidth)
        self.updateRequest.connect(self.updateLineNumberArea)
        self.cursorPositionChanged.connect(self.highlightCurrentLine)
        self.setLineWrapMode(QPlainTextEdit.LineWrapMode.NoWrap)
        self.setReadOnly(True)
        self.setStyleSheet("font-family: Menlo, Consolas, 'Courier New', monospace;")
        self.updateLineNumberAreaWidth(0)
        self.highlightCurrentLine()

    def lineNumberAreaWidth(self) -> int:
        digits = len(str(max(1, self.blockCount())))
        return 10 + self.fontMetrics().horizontalAdvance("9") * digits

    def updateLineNumberAreaWidth(self, _):
        if self._gutter_side == 'left':
            self.setViewportMargins(self.lineNumberAreaWidth(), 0, 0, 0)
        else:
            self.setViewportMargins(0, 0, self.lineNumberAreaWidth(), 0)

    def updateLineNumberArea(self, rect: QRect, dy: int):
        if dy:
            self._lineNumberArea.scroll(0, dy)
        else:
            self._lineNumberArea.update(0, rect.y(), self._lineNumberArea.width(), rect.height())
        if rect.contains(self.viewport().rect()):
            self.updateLineNumberAreaWidth(0)

    def resizeEvent(self, event):  # noqa: D401
        super().resizeEvent(event)
        cr = self.contentsRect()
        if self._gutter_side == 'left':
            self._lineNumberArea.setGeometry(QRect(cr.left(), cr.top(), self.lineNumberAreaWidth(), cr.height()))
        else:
            self._lineNumberArea.setGeometry(QRect(cr.right() - self.lineNumberAreaWidth() + 1, cr.top(), self.lineNumberAreaWidth(), cr.height()))

    def lineNumberAreaPaintEvent(self, event, side: str) -> None:
        painter = QPainter(self._lineNumberArea)
        painter.fillRect(event.rect(), QColor(35, 36, 38))
        block = self.firstVisibleBlock()
        blockNumber = block.blockNumber()
        top = int(self.blockBoundingGeometry(block).translated(self.contentOffset()).top())
        bottom = top + int(self.blockBoundingRect(block).height())

        while block.isValid() and top <= event.rect().bottom():
            if block.isVisible() and bottom >= event.rect().top():
                number = str(blockNumber + 1)
                pen = QColor(150, 150, 155)
                painter.setPen(pen)
                if side == 'left':
                    painter.drawText(0, top, self._lineNumberArea.width() - 4, self.fontMetrics().height(), Qt.AlignmentFlag.AlignRight, number)
                else:
                    painter.drawText(4, top, self._lineNumberArea.width() - 4, self.fontMetrics().height(), Qt.AlignmentFlag.AlignLeft, number)
            block = block.next()
            top = bottom
            bottom = top + int(self.blockBoundingRect(block).height())
            blockNumber += 1

    def highlightCurrentLine(self) -> None:
        pass


class DiffViewer(QWidget):
    def __init__(self, left_bytes: bytes, right_bytes: bytes, left_label: str, right_label: str) -> None:
        super().__init__()
        self._init_ui()
        self.setWindowTitle(f"Diff — {left_label} ⇄ {right_label}")
        self._render(left_bytes, right_bytes)

    def _init_ui(self) -> None:
        layout = QVBoxLayout(self)
        self.split = QSplitter(Qt.Orientation.Horizontal)
        layout.addWidget(self.split)

        # Text widgets
        self.left_text = LineNumberEditor('left')
        self.right_text = LineNumberEditor('right')

        # Image widgets inside scroll areas
        self.left_img_label = QLabel()
        self.right_img_label = QLabel()
        self.left_scroll = QScrollArea()
        self.left_scroll.setWidgetResizable(True)
        self.left_scroll.setWidget(self.left_img_label)
        self.right_scroll = QScrollArea()
        self.right_scroll.setWidgetResizable(True)
        self.right_scroll.setWidget(self.right_img_label)

        # Sync vertical scroll between text editors
        self.left_text.verticalScrollBar().valueChanged.connect(self.right_text.verticalScrollBar().setValue)
        self.right_text.verticalScrollBar().valueChanged.connect(self.left_text.verticalScrollBar().setValue)

    def _set_text_mode(self) -> None:
        # Add 5% spacer between left and right
        self.spacer = QWidget()
        self.spacer.setMinimumWidth(10)
        self.split.addWidget(self.left_text)
        self.split.addWidget(self.spacer)
        self.split.addWidget(self.right_text)
        self.split.setStretchFactor(0, 1)
        self.split.setStretchFactor(1, 0)
        self.split.setStretchFactor(2, 1)

    def _set_image_mode(self) -> None:
        self.spacer = QWidget()
        self.spacer.setMinimumWidth(10)
        self.split.addWidget(self.left_scroll)
        self.split.addWidget(self.spacer)
        self.split.addWidget(self.right_scroll)
        self.split.setStretchFactor(0, 1)
        self.split.setStretchFactor(1, 0)
        self.split.setStretchFactor(2, 1)

    def _render(self, left: bytes, right: bytes) -> None:
        # crude detection: treat as text if decodable in utf-8 or mime is text
        left_is_text = _is_text(left)
        right_is_text = _is_text(right)
        if left_is_text and right_is_text:
            self._set_text_mode()
            left_str = left.decode("utf-8", errors="ignore")
            right_str = right.decode("utf-8", errors="ignore")
            self._render_text_diff(left_str, right_str)
        else:
            self._set_image_mode()
            self._render_images(left, right)

    def _render_text_diff(self, a: str, b: str) -> None:
        a_lines = a.splitlines()
        b_lines = b.splitlines()
        sm = difflib.SequenceMatcher(a=a_lines, b=b_lines)

        def append_formatted(edit: QPlainTextEdit, text: str, bg: Optional[QColor]) -> None:
            cursor = edit.textCursor()
            fmt = QTextCharFormat()
            if bg is not None:
                fmt.setBackground(bg)
            cursor.movePosition(cursor.MoveOperation.End)
            cursor.insertBlock()
            cursor.insertText(text, fmt)

        for tag, i1, i2, j1, j2 in sm.get_opcodes():
            if tag == "equal":
                for k in range(i1, i2):
                    append_formatted(self.left_text, a_lines[k], None)
                for k in range(j1, j2):
                    append_formatted(self.right_text, b_lines[k], None)
            elif tag == "replace":
                for k in range(i1, i2):
                    append_formatted(self.left_text, a_lines[k], QColor(60, 40, 40))
                for k in range(j1, j2):
                    append_formatted(self.right_text, b_lines[k], QColor(40, 60, 40))
            elif tag == "delete":
                for k in range(i1, i2):
                    append_formatted(self.left_text, a_lines[k], QColor(60, 40, 40))
            elif tag == "insert":
                for k in range(j1, j2):
                    append_formatted(self.right_text, b_lines[k], QColor(40, 60, 40))

    def resizeEvent(self, event) -> None:  # noqa: D401
        super().resizeEvent(event)
        # Maintain ~5% spacer
        if self.split.count() == 3:
            total = max(1, self.split.width())
            spacer = max(10, int(total * 0.05))
            side = int((total - spacer) / 2)
            self.split.setSizes([side, spacer, side])

    def _render_images(self, left: bytes, right: bytes) -> None:
        li = QImage.fromData(left)
        ri = QImage.fromData(right)
        self.left_img_label.setPixmap(QPixmap.fromImage(li))
        self.right_img_label.setPixmap(QPixmap.fromImage(ri))


def _is_text(data: bytes) -> bool:
    if not data:
        return True
    kind = filetype.guess(data)
    if kind and kind.MIME.startswith("image/"):
        return False
    try:
        data.decode("utf-8")
        return True
    except Exception:
        return False


