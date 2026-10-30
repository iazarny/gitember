package com.az.gitember.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemanticFormatTest {

    @Test
    void detectsByExtension() {
        assertEquals(SemanticFormat.JSON, SemanticFormat.detect("a/b/package.JSON"));
        assertEquals(SemanticFormat.YAML, SemanticFormat.detect("ci.yml"));
        assertEquals(SemanticFormat.YAML, SemanticFormat.detect("ci.yaml"));
        assertEquals(SemanticFormat.TOML, SemanticFormat.detect("Cargo.toml"));
        assertEquals(SemanticFormat.XML, SemanticFormat.detect("pom.xml"));
        assertEquals(SemanticFormat.INI, SemanticFormat.detect("setup.ini"));
        assertEquals(SemanticFormat.PROPERTIES, SemanticFormat.detect("messages.properties"));
        assertNull(SemanticFormat.detect("Main.java"));
        assertNull(SemanticFormat.detect("Makefile"));
        assertNull(SemanticFormat.detect(null));
    }

    @Test
    void jsonIgnoresKeyOrderAndFormatting() throws Exception {
        String a = "{\"b\": 1, \"a\": {\"y\": [1, 2], \"x\": true}}";
        String b = "{\n  // comment\n  \"a\": {\"x\": true, \"y\": [1,2]},\n  \"b\": 1,\n}";
        assertEquals(SemanticFormat.JSON.canonicalize(a), SemanticFormat.JSON.canonicalize(b));
        assertNotEquals(SemanticFormat.JSON.canonicalize(a),
                SemanticFormat.JSON.canonicalize("{\"b\": 1, \"a\": {\"y\": [2, 1], \"x\": true}}"));
    }

    @Test
    void invalidJsonThrows() {
        assertThrows(Exception.class, () -> SemanticFormat.JSON.canonicalize("{\"a\": "));
        assertThrows(Exception.class, () -> SemanticFormat.JSON.canonicalize("{} {}"));
    }

    @Test
    void blankTextIsEmpty() throws Exception {
        for (SemanticFormat f : SemanticFormat.values()) {
            assertEquals("", f.canonicalize("  \n"));
        }
    }

    @Test
    void yamlIgnoresKeyOrderAndComments() throws Exception {
        String a = "# top\nb: 1\na:\n  x: 'str'\n  y: [1, 2]\n";
        String b = "a:\n  y:\n    - 1\n    - 2\n  x: str   # inline\nb: 1\n";
        String canonical = SemanticFormat.YAML.canonicalize(a);
        assertEquals(canonical, SemanticFormat.YAML.canonicalize(b));
        assertTrue(canonical.indexOf("a:") < canonical.indexOf("b:"), canonical);
    }

    @Test
    void yamlMultiDocument() throws Exception {
        String canonical = SemanticFormat.YAML.canonicalize("a: 1\n---\nb: 2\n");
        assertEquals("a: 1\n---\nb: 2\n", canonical);
    }

    @Test
    void tomlIgnoresKeyOrder() throws Exception {
        String a = "title = \"x\"\n[server]\nport = 80\nhost = \"h\"\n";
        String b = "# c\ntitle=\"x\"\n\n[server]\nhost = \"h\"\nport = 80\n";
        assertEquals(SemanticFormat.TOML.canonicalize(a), SemanticFormat.TOML.canonicalize(b));
    }

    @Test
    void xmlIgnoresAttributeOrderWhitespaceAndComments() throws Exception {
        String a = "<?xml version=\"1.0\"?>\n<root b=\"2\" a=\"1\"><!-- c --><item>  text </item><empty></empty></root>";
        String b = "<root a=\"1\"   b=\"2\">\n    <item>text</item>\n    <empty/>\n</root>\n";
        String canonical = SemanticFormat.XML.canonicalize(a);
        assertEquals(canonical, SemanticFormat.XML.canonicalize(b));
        assertEquals("<root a=\"1\" b=\"2\">\n  <item>text</item>\n  <empty/>\n</root>\n", canonical);
    }

    @Test
    void xmlKeepsElementOrder() throws Exception {
        assertNotEquals(SemanticFormat.XML.canonicalize("<r><a/><b/></r>"),
                SemanticFormat.XML.canonicalize("<r><b/><a/></r>"));
    }

    @Test
    void xmlDoesNotResolveExternalEntities() throws Exception {
        String xxe = "<!DOCTYPE r [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><r>&x;</r>";
        assertFalse(SemanticFormat.XML.canonicalize(xxe).contains("root:"));
    }

    @Test
    void iniSortsSectionsAndKeys() throws Exception {
        String a = "; c\nglobal=1\n[b]\nz = 1\ny=2\n[a]\nk: v\nflag\n";
        String b = "global = 1\n[a]\nflag\nk = v\n# c\n[b]\ny = 2\nz=1\n";
        String canonical = SemanticFormat.INI.canonicalize(a);
        assertEquals(canonical, SemanticFormat.INI.canonicalize(b));
        assertEquals("global = 1\n\n[a]\nflag\nk = v\n\n[b]\ny = 2\nz = 1\n", canonical);
    }

    @Test
    void propertiesSortsKeysAndResolvesSyntax() throws Exception {
        String a = "b=2\n# c\na = 1\nlong=x\\\n  y\n";
        String b = "a:1\nlong = xy\nb 2\n";
        String canonical = SemanticFormat.PROPERTIES.canonicalize(a);
        assertEquals(canonical, SemanticFormat.PROPERTIES.canonicalize(b));
        assertEquals("a=1\nb=2\nlong=xy\n", canonical);
    }
}
