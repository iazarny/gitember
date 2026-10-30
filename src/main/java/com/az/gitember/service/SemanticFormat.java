package com.az.gitember.service;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * Structured file formats that can be compared semantically.
 * <p>
 * {@link #canonicalize(String)} parses the text and re-renders it in a canonical form
 * (sorted keys / attributes, normalised formatting, no comments), so a plain line diff
 * of two canonical texts shows only meaningful changes.
 */
public enum SemanticFormat {

    JSON("json"),
    YAML("yaml", "yml"),
    TOML("toml"),
    XML("xml", "xsd", "xsl", "xslt", "fxml", "wsdl", "svg"),
    INI("ini"),
    PROPERTIES("properties");

    private final String[] extensions;

    SemanticFormat(String... extensions) {
        this.extensions = extensions;
    }

    /** Returns the format for the file name's extension, or {@code null} when not supported. */
    public static SemanticFormat detect(String fileName) {
        if (fileName == null) return null;
        String lower = fileName.toLowerCase();
        int dot = lower.lastIndexOf('.');
        if (dot < 0 || dot == lower.length() - 1) return null;
        String ext = lower.substring(dot + 1);
        for (SemanticFormat f : values()) {
            for (String e : f.extensions) {
                if (e.equals(ext)) return f;
            }
        }
        return null;
    }

    /**
     * Parses {@code text} and returns its canonical representation.
     * Blank text (e.g. an added or deleted file) canonicalizes to an empty string.
     *
     * @throws Exception when the text is not valid for this format
     */
    public String canonicalize(String text) throws Exception {
        if (text == null || text.isBlank()) return "";
        return switch (this) {
            case JSON -> canonicalJson(text);
            case YAML -> canonicalYaml(text);
            case TOML -> canonicalToml(text);
            case XML -> canonicalXml(text);
            case INI -> canonicalIni(text);
            case PROPERTIES -> canonicalProperties(text);
        };
    }

    // ---- JSON / YAML / TOML (Jackson tree) ----

    private static final ObjectMapper JSON_MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    private static final YAMLMapper YAML_MAPPER = new YAMLMapper(YAMLFactory.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
            .build());

    private static final TomlMapper TOML_MAPPER = new TomlMapper();

    private static String canonicalJson(String text) throws Exception {
        JsonNode tree = JSON_MAPPER.readTree(text);
        return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(sorted(tree)) + "\n";
    }

    private static String canonicalYaml(String text) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (var docs = YAML_MAPPER.readerFor(JsonNode.class).<JsonNode>readValues(text)) {
            boolean first = true;
            while (docs.hasNextValue()) {
                JsonNode doc = docs.nextValue();
                if (doc == null || doc.isMissingNode()) continue;
                if (!first) sb.append("---\n");
                sb.append(YAML_MAPPER.writeValueAsString(sorted(doc)));
                first = false;
            }
        }
        return sb.toString();
    }

    private static String canonicalToml(String text) throws Exception {
        JsonNode tree = TOML_MAPPER.readTree(text);
        return TOML_MAPPER.writeValueAsString(sorted(tree));
    }

    /** Deep copy of {@code node} with object keys in sorted order; array order is kept. */
    private static JsonNode sorted(JsonNode node) {
        if (node.isObject()) {
            Map<String, JsonNode> fields = new TreeMap<>();
            node.fields().forEachRemaining(e -> fields.put(e.getKey(), sorted(e.getValue())));
            ObjectNode out = JsonNodeFactory.instance.objectNode();
            fields.forEach(out::set);
            return out;
        }
        if (node.isArray()) {
            ArrayNode out = JsonNodeFactory.instance.arrayNode();
            node.forEach(child -> out.add(sorted(child)));
            return out;
        }
        return node;
    }

    // ---- XML ----


    public static String canonicalXml(String text) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler(null);
        Document doc = db.parse(new InputSource(new StringReader(text)));

        // 1. Remove all pure-whitespace text nodes to eliminate empty lines
        removeWhitespaceNodes(doc.getDocumentElement());

        // 2. Sort the element tree recursively
        sortElementTree(doc.getDocumentElement());

        // 3. Serialize back to a clean String without "standalone" attributes
        return documentToString(doc);
    }

    private static void removeWhitespaceNodes(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                // If text is only whitespace/newlines, remove it
                if (node.getNodeValue().trim().isEmpty()) {
                    element.removeChild(node);
                }
            } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                removeWhitespaceNodes((Element) node);
            }
        }
    }

    private static void sortElementTree(Element element) {
        NodeList children = element.getChildNodes();
        List<Element> childElements = new ArrayList();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                childElements.add((Element) node);
                sortElementTree((Element) node);
            }
        }

        // Sort child elements alphabetically by tag name
        childElements.sort(Comparator.comparing(Node::getNodeName));

        // Re-attach elements in the sorted order
        for (Element child : childElements) {
            element.removeChild(child);
            element.appendChild(child);
        }
    }

    private static String documentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // Omit built-in declaration to avoid unwanted "standalone='no'"
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        // Prepend a clean, standard XML declaration
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + writer.toString().trim();
    }

    private static String _canonicalXml(String text) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler(null);   // don't print parse errors to stderr; they are thrown
        Document doc = db.parse(new InputSource(new StringReader(text)));

        StringBuilder sb = new StringBuilder();
        writeElement(doc.getDocumentElement(), 0, sb);
        return sb.toString();
    }

    private static void writeElement(Element e, int depth, StringBuilder sb) {
        String indent = "  ".repeat(depth);
        sb.append(indent).append('<').append(e.getTagName());

        NamedNodeMap attrs = e.getAttributes();
        Map<String, String> sortedAttrs = new TreeMap<>();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            sortedAttrs.put(a.getName(), a.getValue());
        }
        sortedAttrs.forEach((k, v) -> sb.append(' ').append(k).append("=\"").append(escapeXml(v, true)).append('"'));

        // Children: elements plus trimmed, merged, non-empty text runs
        List<Object> children = new ArrayList<>();
        StringBuilder textRun = new StringBuilder();
        NodeList nodes = e.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            switch (n.getNodeType()) {
                case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> textRun.append(n.getNodeValue());
                case Node.ENTITY_REFERENCE_NODE -> textRun.append(n.getTextContent());
                case Node.ELEMENT_NODE -> {
                    flushText(textRun, children);
                    children.add(n);
                }
                default -> { /* comments, processing instructions: not semantic */ }
            }
        }
        flushText(textRun, children);

        if (children.isEmpty()) {
            sb.append("/>\n");
        } else if (children.size() == 1 && children.get(0) instanceof String s && s.indexOf('\n') < 0) {
            sb.append('>').append(escapeXml(s, false)).append("</").append(e.getTagName()).append(">\n");
        } else {
            sb.append(">\n");
            String childIndent = "  ".repeat(depth + 1);
            for (Object child : children) {
                if (child instanceof Element ce) {
                    writeElement(ce, depth + 1, sb);
                } else {
                    for (String line : ((String) child).split("\n")) {
                        sb.append(childIndent).append(escapeXml(line, false)).append('\n');
                    }
                }
            }
            sb.append(indent).append("</").append(e.getTagName()).append(">\n");
        }
    }

    /** Adds the accumulated text (each line trimmed, blank lines dropped) as a child, then clears it. */
    private static void flushText(StringBuilder textRun, List<Object> children) {
        if (textRun.isEmpty()) return;
        StringBuilder normalized = new StringBuilder();
        for (String line : textRun.toString().split("\r?\n|\r")) {
            String t = line.strip();
            if (t.isEmpty()) continue;
            if (!normalized.isEmpty()) normalized.append('\n');
            normalized.append(t);
        }
        if (!normalized.isEmpty()) children.add(normalized.toString());
        textRun.setLength(0);
    }

    private static String escapeXml(String s, boolean attr) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append(attr ? "&quot;" : "\"");
                case '\n' -> sb.append(attr ? "&#10;" : "\n");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---- INI ----

    private static String canonicalIni(String text) {
        // section name -> (key -> value or null for a bare key); "" is the global section
        Map<String, Map<String, String>> sections = new TreeMap<>();
        Map<String, String> current = sections.computeIfAbsent("", k -> new TreeMap<>());
        for (String raw : text.split("\r?\n|\r")) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                String name = line.substring(1, line.length() - 1).strip();
                current = sections.computeIfAbsent(name, k -> new TreeMap<>());
                continue;
            }
            int sep = indexOfSeparator(line);
            if (sep < 0) {
                current.put(line, null);
            } else {
                current.put(line.substring(0, sep).strip(), line.substring(sep + 1).strip());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, String>> section : sections.entrySet()) {
            boolean global = section.getKey().isEmpty();
            if (global && section.getValue().isEmpty()) continue;
            if (!sb.isEmpty()) sb.append('\n');
            if (!global) sb.append('[').append(section.getKey()).append("]\n");
            section.getValue().forEach((k, v) ->
                    sb.append(k).append(v == null ? "" : " = " + v).append('\n'));
        }
        return sb.toString();
    }

    private static int indexOfSeparator(String line) {
        int eq = line.indexOf('=');
        int colon = line.indexOf(':');
        if (eq < 0) return colon;
        if (colon < 0) return eq;
        return Math.min(eq, colon);
    }

    // ---- Properties ----

    private static String canonicalProperties(String text) throws Exception {
        Properties props = new Properties();
        props.load(new StringReader(text));
        Map<String, String> sortedProps = new TreeMap<>();
        props.forEach((k, v) -> sortedProps.put(k.toString(), v.toString()));
        StringBuilder sb = new StringBuilder();
        sortedProps.forEach((k, v) -> sb.append(escapeProperty(k)).append('=').append(escapeProperty(v)).append('\n'));
        return sb.toString();
    }

    /** Escapes only what would otherwise break the one-entry-per-line layout. */
    private static String escapeProperty(String s) {
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
