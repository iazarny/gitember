from __future__ import annotations

import difflib
from typing import Optional, List

import filetype
from PyQt6.QtCore import Qt, QRect, QPoint, pyqtSignal
from PyQt6.QtGui import QTextCharFormat, QColor, QImage, QPixmap, QPainter, QPainterPath, QMouseEvent
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


class MinimapWidget(QWidget):
    def __init__(self, editor: "LineNumberEditor", line_map: List[tuple[str, int]]) -> None:
        super().__init__()
        self._editor = editor
        self._line_map = line_map  # (status, line_count)
        self.setMinimumWidth(20)
        self.setMaximumWidth(40)
        self.setMouseTracking(True)
        # Sync scroll
        self._editor.verticalScrollBar().valueChanged.connect(lambda *_: self.update())

    def paintEvent(self, event) -> None:  # noqa: D401
        painter = QPainter(self)
        painter.fillRect(self.rect(), QColor(25, 26, 28))

        total_lines = sum(cnt for _, cnt in self._line_map)
        if total_lines == 0:
            return

        h = self.height()
        y = 0
        for status, cnt in self._line_map:
            if cnt > 0:
                seg_h = int(h * cnt / total_lines)
                if seg_h == 0 and cnt > 0:
                    seg_h = 1
                color = self._color_for_status(status)
                painter.fillRect(0, y, self.width(), seg_h, color)
                y += seg_h

        # Viewport indicator
        self._draw_viewport(painter, h, total_lines)

    def _color_for_status(self, status: str) -> QColor:
        if status == "deleted":
            return QColor(231, 76, 60, 180)  # red
        elif status == "inserted":
            return QColor(46, 204, 113, 180)  # green
        elif status == "modified":
            return QColor(70, 130, 180, 180)  # blue
        return QColor(100, 100, 105, 120)  # grey for equal

    def _draw_viewport(self, painter: QPainter, height: int, total_lines: int) -> None:
        if total_lines == 0:
            return
        vsb = self._editor.verticalScrollBar()
        vmin = vsb.minimum()
        vmax = vsb.maximum()
        vval = vsb.value()
        if vmax == vmin:
            return
        # Map scroll position to line range
        vport_h = self._editor.viewport().height()
        doc_h = self._editor.document().size().height()
        visible_start = vval / (vmax - vmin) if (vmax > vmin) else 0
        visible_end = min(1.0, visible_start + vport_h / max(doc_h, 1))
        y1 = int(height * visible_start)
        y2 = int(height * visible_end)
        painter.setPen(QColor(255, 255, 255, 100))
        painter.drawRect(0, y1, self.width() - 1, y2 - y1)
        painter.fillRect(0, y1, self.width(), y2 - y1, QColor(255, 255, 255, 30))

    def mousePressEvent(self, event: QMouseEvent) -> None:
        if event.button() == Qt.MouseButton.LeftButton:
            h = self.height()
            total_lines = sum(cnt for _, cnt in self._line_map)
            if total_lines > 0:
                y = event.position().y()
                line = int(total_lines * y / h)
                self._jump_to_line(line)

    def _jump_to_line(self, target_line: int) -> None:
        block = self._editor.document().findBlockByNumber(target_line)
        if block.isValid():
            cursor = self._editor.textCursor()
            cursor.setPosition(block.position())
            self._editor.setTextCursor(cursor)
            self._editor.centerCursor()

    def update_line_map(self, line_map: List[tuple[str, int]]) -> None:
        """Update the line map and trigger repaint."""
        self._line_map = line_map
        self.update()


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
        # Create minimap with empty line map initially (will be updated in _render_text_diff)
        self._left_minimap = MinimapWidget(self.left_text, [])

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
        # Add 5% spacer between left and right (custom painter for Bezier bands)
        self.spacer = DiffSpacer()
        self.spacer.setMinimumWidth(10)
        # Add all widgets: minimap, left panel, spacer, right panel
        self.split.addWidget(self._left_minimap)
        self.split.addWidget(self.left_text)
        self.split.addWidget(self.spacer)
        self.split.addWidget(self.right_text)
        self.split.setStretchFactor(0, 0)  # minimap fixed width
        self.split.setStretchFactor(1, 1)  # left panel
        self.split.setStretchFactor(2, 0)  # spacer
        self.split.setStretchFactor(3, 1)  # right panel

    def _set_image_mode(self) -> None:
        self.spacer = DiffSpacer()
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

        opcodes = sm.get_opcodes()
        # feed spacer for Bezier painting
        if isinstance(self.spacer, DiffSpacer):
            self.spacer.configure(self.left_text, self.right_text, opcodes)

        # Build line map for minimap
        left_lines: List[tuple[str, int]] = []
        for tag, i1, i2, j1, j2 in opcodes:
            if tag == "equal":
                cnt = i2 - i1
                left_lines.append(("equal", cnt))
            elif tag == "replace":
                left_lines.append(("modified", i2 - i1))
            elif tag == "delete":
                left_lines.append(("deleted", i2 - i1))
            # insert doesn't affect left file

        # Update minimap with line map
        self._left_minimap.update_line_map(left_lines)

        for tag, i1, i2, j1, j2 in opcodes:
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

    def _render_images(self, left: bytes, right: bytes) -> None:
        li = QImage.fromData(left)
        ri = QImage.fromData(right)
        self.left_img_label.setPixmap(QPixmap.fromImage(li))
        self.right_img_label.setPixmap(QPixmap.fromImage(ri))

    def resizeEvent(self, event) -> None:  # noqa: D401
        super().resizeEvent(event)
        # Maintain proper width distribution: 5% minimap, 40% left, 5% gutter, 40% right
        count = self.split.count()
        total = max(1, self.split.width())
        if count == 4:  # with left minimap
            minimap_w = int(total * 0.05)
            spacer_w = int(total * 0.05)
            panel_w = int(total * 0.40)
            # Right panel gets remaining space
            right_panel_w = total - minimap_w - spacer_w - panel_w
            self.split.setSizes([minimap_w, panel_w, spacer_w, right_panel_w])
        elif count == 3:  # without minimap (image mode)
            spacer = int(total * 0.05)
            side = int((total - spacer) / 2)
            self.split.setSizes([side, spacer, side])


class DiffSpacer(QWidget):
    def __init__(self) -> None:
        super().__init__()
        self._left: LineNumberEditor | None = None
        self._right: LineNumberEditor | None = None
        self._opcodes = []

    def configure(self, left: LineNumberEditor, right: LineNumberEditor, opcodes) -> None:
        self._left = left
        self._right = right
        self._opcodes = opcodes
        # trigger repaint on scroll
        left.updateRequest.connect(lambda *_: self.update())
        right.updateRequest.connect(lambda *_: self.update())
        self.update()

    def paintEvent(self, event) -> None:  # noqa: D401
        if not (self._left and self._right and self._opcodes):
            return
        painter = QPainter(self)
        painter.fillRect(self.rect(), QColor(30, 31, 33))

        def y_for(editor: LineNumberEditor, line_index: int) -> int:
            # map 0-based logical line index to y within editor viewport
            block = editor.document().findBlockByNumber(line_index)
            top = editor.blockBoundingGeometry(block).translated(editor.contentOffset()).top()
            # translate into our coordinates: need relative to widget top
            # We align centers of editors; assume same line height font
            return int(top)

        lh_left = self._left.fontMetrics().height()
        lh_right = self._right.fontMetrics().height()
        center_left = self.mapFrom(self._left.viewport(), self._left.viewport().rect().topLeft()).y()
        center_right = self.mapFrom(self._right.viewport(), self._right.viewport().rect().topLeft()).y()

        for tag, i1, i2, j1, j2 in self._opcodes:
            if tag == 'equal':
                continue
            # y ranges
            ly1 = center_left + y_for(self._left, i1)
            ly2 = center_left + y_for(self._left, max(i2 - 1, i1)) + lh_left
            ry1 = center_right + y_for(self._right, j1)
            ry2 = center_right + y_for(self._right, max(j2 - 1, j1)) + lh_right

            # ensure minimal height for inserts/deletes
            if i1 == i2:  # insert
                ly1 = ly2 = center_left + y_for(self._left, max(i1 - 1, 0)) + lh_left // 2
            if j1 == j2:  # delete
                ry1 = ry2 = center_right + y_for(self._right, max(j1 - 1, 0)) + lh_right // 2

            color = QColor(70, 130, 180, 110)  # blue for replace
            if tag == 'insert':
                color = QColor(46, 204, 113, 110)  # green
            elif tag == 'delete':
                color = QColor(231, 76, 60, 110)  # red

            path = QPainterPath()
            x0 = 0
            x1 = self.width()
            path.moveTo(x0, ly1)
            path.cubicTo(x0 + self.width() * 0.4, ly1, x1 - self.width() * 0.4, ry1, x1, ry1)
            path.lineTo(x1, ry2)
            path.cubicTo(x1 - self.width() * 0.4, ry2, x0 + self.width() * 0.4, ly2, x0, ly2)
            path.closeSubpath()
            painter.fillPath(path, color)


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


