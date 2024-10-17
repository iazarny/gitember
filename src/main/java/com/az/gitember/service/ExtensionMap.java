package com.az.gitember.service;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

public class ExtensionMap {

    private static HashMap<String, String> map = new HashMap<>(50);

    static {
        add(new ExtensionInfo("text/plain", "sql", true));
        add(new ExtensionInfo("text/plain", "cs", true));
        add(new ExtensionInfo("text/plain", "csharp", true));
        add(new ExtensionInfo("text/plain", "sh", true));
        add(new ExtensionInfo("text/plain", "cmd", true));
        add(new ExtensionInfo("text/plain", "bat", true));
        add(new ExtensionInfo("text/plain", "vm", true));
        add(new ExtensionInfo("text/plain", "tpl", true));
        add(new ExtensionInfo("text/plain", "asp", true));
        add(new ExtensionInfo("text/plain", "aspx", true));
        add(new ExtensionInfo("text/yaml", "yaml", true));
        add(new ExtensionInfo("text/yaml", "yml", true));
        add(new ExtensionInfo("text/calendar", "ics", true));
        add(new ExtensionInfo("text/calendar", "ifb", true));
        add(new ExtensionInfo("text/css", "css", true));
        add(new ExtensionInfo("text/csv", "csv", true));
        add(new ExtensionInfo("text/html", "html", true));
        add(new ExtensionInfo("text/html", "htm", true));
        add(new ExtensionInfo("text/plain", "txt", true));

        add(new ExtensionInfo("text/plain", "proto", true));
        add(new ExtensionInfo("text/plain", "avro", true));

        add(new ExtensionInfo("text/plain", "hrl", true));
        add(new ExtensionInfo("text/plain", "erl", true));
        add(new ExtensionInfo("text/plain", "ex", true));
        add(new ExtensionInfo("text/plain", "exs", true));
        add(new ExtensionInfo("text/plain", "kt", true));
        add(new ExtensionInfo("text/plain", "kts", true));
        add(new ExtensionInfo("text/plain", "ktm", true));
        add(new ExtensionInfo("text/plain", "properties", true));
        add(new ExtensionInfo("text/plain", "props", true));
        add(new ExtensionInfo("text/plain", "prop", true));
        add(new ExtensionInfo("text/plain", "text", true));
        add(new ExtensionInfo("text/plain", "conf", true));
        add(new ExtensionInfo("text/plain", "def", true));
        add(new ExtensionInfo("text/plain", "scala", true));
        add(new ExtensionInfo("text/plain", "sc", true));
        add(new ExtensionInfo("text/plain", "swift", true));

        add(new ExtensionInfo("text/plain", "bas", true));
        add(new ExtensionInfo("text/plain", "vb", true));
        add(new ExtensionInfo("text/plain", "vba", true));
        add(new ExtensionInfo("text/plain", "vbs", true));
        add(new ExtensionInfo("text/plain", "vbscript", true));
        add(new ExtensionInfo("text/plain", "vbe", true));
        add(new ExtensionInfo("text/plain", "wsf", true));
        add(new ExtensionInfo("text/plain", "wsc", true));


        add(new ExtensionInfo("text/plain", "md", true));
        add(new ExtensionInfo("text/plain", "go", true));
        add(new ExtensionInfo("text/plain", "golang", true));
        add(new ExtensionInfo("text/plain", "rb", true));
        add(new ExtensionInfo("text/plain", "lua", true));
        add(new ExtensionInfo("text/plain", "php", true));
        add(new ExtensionInfo("text/plain", "dart", true));
        add(new ExtensionInfo("text/plain", "list", true));
        add(new ExtensionInfo("text/x-log", "log", true));
        add(new ExtensionInfo("text/plain", "in", true));
        add(new ExtensionInfo("text/prs.lines.tag", "dsc", true));
        add(new ExtensionInfo("text/richtext", "rtx", true));
        add(new ExtensionInfo("text/sgml", "sgml", true));
        add(new ExtensionInfo("text/sgml", "sgm", true));
        add(new ExtensionInfo("text/tab-separated-values", "tsv", true));
        add(new ExtensionInfo("text/troff", "t", true));
        add(new ExtensionInfo("text/troff", "tr", true));
        add(new ExtensionInfo("text/troff", "roff", true));
        add(new ExtensionInfo("text/troff", "man", true));
        add(new ExtensionInfo("text/troff", "me", true));
        add(new ExtensionInfo("text/troff", "ms", true));
        add(new ExtensionInfo("text/uri-list", "uri", true));
        add(new ExtensionInfo("text/uri-list", "uris", true));
        add(new ExtensionInfo("text/uri-list", "urls", true));
        add(new ExtensionInfo("text/vnd.curl", "curl", true));
        add(new ExtensionInfo("text/vnd.curl.dcurl", "dcurl", true));
        add(new ExtensionInfo("text/vnd.curl.scurl", "scurl", true));
        add(new ExtensionInfo("text/vnd.curl.mcurl", "mcurl", true));
        add(new ExtensionInfo("text/vnd.fly", "fly", true));
        add(new ExtensionInfo("text/vnd.fmi.flexstor", "flx", true));
        add(new ExtensionInfo("text/vnd.graphviz", "gv", true));
        add(new ExtensionInfo("text/vnd.in3d.3dml", "3dml", true));
        add(new ExtensionInfo("text/vnd.in3d.spot", "spot", true));
        add(new ExtensionInfo("text/vnd.sun.j2me.app-descriptor", "jad", true));
        add(new ExtensionInfo("text/vnd.wap.wml", "wml", true));
        add(new ExtensionInfo("text/vnd.wap.wmlscript", "wmls", true));
        add(new ExtensionInfo("text/x-assembly", "s", true));
        add(new ExtensionInfo("text/x-assembly", "asm", true));
        add(new ExtensionInfo("text/x-assembly", "masm", true));
        add(new ExtensionInfo("text/x-csrc", "c", true));
        add(new ExtensionInfo("text/x-c++src", "cc", true));
        add(new ExtensionInfo("text/x-c++src", "cxx", true));
        add(new ExtensionInfo("text/x-c++src", "cpp", true));
        add(new ExtensionInfo("text/x-chdr", "h", true));
        add(new ExtensionInfo("text/x-c++hdr", "hh", true));
        add(new ExtensionInfo("text/x-fortran", "f", true));
        add(new ExtensionInfo("text/x-fortran", "for", true));
        add(new ExtensionInfo("text/x-fortran", "f77", true));
        add(new ExtensionInfo("text/x-fortran", "f03", true));
        add(new ExtensionInfo("text/x-fortran", "f90", true));
        add(new ExtensionInfo("text/x-pascal", "p", true));
        add(new ExtensionInfo("text/x-pascal", "pas", true));
        add(new ExtensionInfo("text/x-pascal", "py", true));
        add(new ExtensionInfo("text/x-java-source", "java", true));
        add(new ExtensionInfo("text/x-java-source", "jsp", true));
        add(new ExtensionInfo("text/x-java-source", "jspx", true));
        add(new ExtensionInfo("text/x-setext", "etx", true));
        add(new ExtensionInfo("text/x-uuencode", "uu", true));
        add(new ExtensionInfo("text/x-vcalendar", "vcs", true));
        add(new ExtensionInfo("text/x-vcard", "vcf", true));
        add(new ExtensionInfo("application/javascript", "js", true));
        add(new ExtensionInfo("application/typescript", "ts", true));
        add(new ExtensionInfo("application/javascript", "jsx", true));
        add(new ExtensionInfo("application/json", "json", true));
        add(new ExtensionInfo("application/lost+xml", "lostxml", true));
        add(new ExtensionInfo("application/atom+xml", "atom", true));
        add(new ExtensionInfo("application/atomcat+xml", "atomcat", true));
        add(new ExtensionInfo("application/atomsvc+xml", "atomsvc", true));
        add(new ExtensionInfo("application/ccxml+xml", "ccxml", true));
        add(new ExtensionInfo("application/emma+xml", "emma", true));
        add(new ExtensionInfo("application/mathematica", "ma", true));
        add(new ExtensionInfo("application/mathematica", "nb", true));
        add(new ExtensionInfo("application/mathematica", "mb", true));
        add(new ExtensionInfo("application/mathematica", "mat", true));
        add(new ExtensionInfo("application/mathml+xml", "mathml", true));
        add(new ExtensionInfo("text/fxml", "fxml", true));
        add(new ExtensionInfo("application/xhtml+xml", "xhtml", true));
        add(new ExtensionInfo("application/xhtml+xml", "xht", true));
        add(new ExtensionInfo("application/xml", "xml", true));
        add(new ExtensionInfo("application/xml", "xsl", true));
        add(new ExtensionInfo("application/xml-dtd", "dtd", true));
        add(new ExtensionInfo("application/xop+xml", "xop", true));
        add(new ExtensionInfo("application/xslt+xml", "xslt", true));
        add(new ExtensionInfo("application/xspf+xml", "xspf", true));
        add(new ExtensionInfo("application/xv+xml", "mxml", true));
        add(new ExtensionInfo("application/xv+xml", "xhvml", true));
        add(new ExtensionInfo("application/xv+xml", "xvml", true));
        add(new ExtensionInfo("application/xv+xml", "xvm", true));
        add(new ExtensionInfo("application/ecmascript", "ecma", true));
        add(new ExtensionInfo("application/sbml+xml", "sbml", true));
        add(new ExtensionInfo("application/rdf+xml", "rdf", true));
        add(new ExtensionInfo("application/reginfo+xml", "rif", true));
        add(new ExtensionInfo("application/resource-lists+xml", "rl", true));
        add(new ExtensionInfo("application/resource-lists-diff+xml", "rld", true));
        add(new ExtensionInfo("application/rls-services+xml", "rs", true));
        add(new ExtensionInfo("application/rsd+xml", "rsd", true));
        add(new ExtensionInfo("application/rss+xml", "rss", true));
        add(new ExtensionInfo("application/shf+xml", "shf", true));
        add(new ExtensionInfo("application/smil+xml", "smi", true));
        add(new ExtensionInfo("application/smil+xml", "smil", true));
        add(new ExtensionInfo("application/sparql-query", "rq", true));
        add(new ExtensionInfo("application/sparql-results+xml", "srx", true));
        add(new ExtensionInfo("application/srgs+xml", "grxml", true));
        add(new ExtensionInfo("application/ssml+xml", "ssml", true));
        add(new ExtensionInfo("application/vnd.chemdraw+xml", "cdxml", true));
        add(new ExtensionInfo("application/vnd.criticaltools.wbs+xml", "wbs", true));
        add(new ExtensionInfo("text/x-scheme", "scm", true));
        add(new ExtensionInfo("image/svg+xml", "svg", true));
        add(new ExtensionInfo("image/vnd.dxf", "dxf", true));
        add(new ExtensionInfo("image/x-xpixmap", "xpm", true));
        add(new ExtensionInfo("message/rfc822", "eml", true));
        add(new ExtensionInfo("message/rfc822", "mime", true));
        add(new ExtensionInfo("text/x-patch", "dif", true));
        add(new ExtensionInfo("text/x-patch", "diff", true));
        add(new ExtensionInfo("text/x-patch", "patch", true));
        add(new ExtensionInfo("text/plain", "tf", true));
        add(new ExtensionInfo("application/json", "libsonnet", true));
        add(new ExtensionInfo("application/json", "jsonnet", true));
        add(new ExtensionInfo("application/x-typescript", "tsx", true));

    }

    public static void add(ExtensionInfo ez) {
        
        map.put(ez.getFileExtension(), ez.getFileExtension());

    }
    
    public static boolean isTextExtension(Path path) {
        String ext = getExtension(path.toString());
        return map.containsKey(ext);
    }

    public static boolean isTextExtension(String file) {
        String ext = getExtension(file);
        return map.containsKey(ext);
    }

    public static String getExtension(String fileName) {

        char ch;
        int len;
        if (fileName == null ||
                (len = fileName.length()) == 0 ||
                (ch = fileName.charAt(len - 1)) == '/' || ch == '\\' || //in the case of a directory
                ch == '.') //in the case of . or ..
            return "";
        int dotInd = fileName.lastIndexOf('.'),
                sepInd = Math.max(fileName.lastIndexOf(File.separatorChar), fileName.lastIndexOf('\\'));
        if (dotInd <= sepInd)
            return "";
        else
            return fileName.substring(dotInd + 1).toLowerCase();
    }

}
