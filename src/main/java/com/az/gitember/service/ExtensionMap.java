package com.az.gitember.service;

import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;

public class ExtensionMap {

    private static HashMap<String, ExtensionInfo> map = new HashMap<>(50);
    


    static {

        add(new ExtensionInfo( "image/jpeg", "jpg" , ExtensionInfo.ExtType.IMAGE ));
        add(new ExtensionInfo( "image/jpeg", "jpeg" , ExtensionInfo.ExtType.IMAGE ));
        add(new ExtensionInfo( "image/png", "png" , ExtensionInfo.ExtType.IMAGE ));
        add(new ExtensionInfo( "image/webp", "webp" , ExtensionInfo.ExtType.IMAGE ));
        add(new ExtensionInfo( "image/gif", "gif" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/avif", "avif" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/svg+xml", "svg" , ExtensionInfo.ExtType.IMAGE ));
        add(new ExtensionInfo( "image/tiff", "tiff" , ExtensionInfo.ExtType.IMAGE ));
        add(new ExtensionInfo( "image/bmp", "bmp" , ExtensionInfo.ExtType.IMAGE ));

        //add(new ExtensionInfo( "image/heic", "heic" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/heif", "heif" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/jxl", "jxl" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/apng", "apng" , ExtensionInfo.ExtType.IMAGE ));

        //add(new ExtensionInfo( "image/vnd.adobe.photoshop", "psd" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "application/postscript", "ai" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "application/postscript", "eps" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/x-canon-cr2", "cr2" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/x-nikon-nef", "nef" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/x-icon", "ico" , ExtensionInfo.ExtType.IMAGE ));
        //add(new ExtensionInfo( "image/raw", "raw" , ExtensionInfo.ExtType.IMAGE ));

        add(new ExtensionInfo("text/plain", "sql", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "cs", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "csharp", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "sh", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "cmd", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "bat", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "vm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "tpl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "asp", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "aspx", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/yaml", "yaml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/yaml", "yml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/calendar", "ics", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/calendar", "ifb", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/css", "css", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/csv", "csv", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/html", "html", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/html", "htm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "txt", ExtensionInfo.ExtType.TEXT));

        add(new ExtensionInfo("text/plain", "proto", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "avro", ExtensionInfo.ExtType.TEXT));

        add(new ExtensionInfo("text/plain", "hrl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "erl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "ex", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "exs", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "kt", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "kts", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "ktm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "properties", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "props", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "prop", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "text", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "conf", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "config", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "def", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "scala", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "sc", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "swift", ExtensionInfo.ExtType.TEXT));

        add(new ExtensionInfo("text/plain", "bas", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "vb", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "vba", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "vbs", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "vbscript", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "vbe", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "wsf", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "wsc", ExtensionInfo.ExtType.TEXT));


        add(new ExtensionInfo("text/plain", "md", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "gitattributes", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "go", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "golang", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "rb", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "lua", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "php", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "dart", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "list", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-log", "log", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "in", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/prs.lines.tag", "dsc", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/richtext", "rtx", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/sgml", "sgml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/sgml", "sgm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/tab-separated-values", "tsv", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/troff", "t", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/troff", "tr", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/troff", "roff", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/troff", "man", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/troff", "me", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/troff", "ms", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/uri-list", "uri", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/uri-list", "uris", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/uri-list", "urls", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.curl", "curl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.curl.dcurl", "dcurl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.curl.scurl", "scurl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.curl.mcurl", "mcurl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.fly", "fly", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.fmi.flexstor", "flx", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.graphviz", "gv", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.in3d.3dml", "3dml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.in3d.spot", "spot", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.sun.j2me.app-descriptor", "jad", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.wap.wml", "wml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/vnd.wap.wmlscript", "wmls", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-assembly", "s", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-assembly", "asm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-assembly", "masm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-csrc", "c", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-c++src", "cc", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-c++src", "cxx", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-c++src", "cpp", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-chdr", "h", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-c++hdr", "hh", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-c++hdr", "hpp", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-fortran", "f", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-fortran", "for", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-fortran", "f77", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-fortran", "f03", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-fortran", "f90", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-pascal", "p", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-pascal", "pas", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-pascal", "py", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-java-source", "java", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-java-source", "jsp", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-java-source", "jspx", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-setext", "etx", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-uuencode", "uu", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-vcalendar", "vcs", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-vcard", "vcf", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/javascript", "js", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/typescript", "ts", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/javascript", "jsx", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/json", "json", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/lost+xml", "lostxml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/atom+xml", "atom", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/atomcat+xml", "atomcat", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/atomsvc+xml", "atomsvc", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/ccxml+xml", "ccxml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/emma+xml", "emma", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/mathematica", "ma", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/mathematica", "nb", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/mathematica", "mb", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/mathematica", "mat", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/mathml+xml", "mathml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/fxml", "fxml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xhtml+xml", "xhtml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xhtml+xml", "xht", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xml", "xml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xml", "xsl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xml-dtd", "dtd", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xop+xml", "xop", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xslt+xml", "xslt", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xspf+xml", "xspf", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xv+xml", "mxml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xv+xml", "xhvml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xv+xml", "xvml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/xv+xml", "xvm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/ecmascript", "ecma", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/sbml+xml", "sbml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/rdf+xml", "rdf", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/reginfo+xml", "rif", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/resource-lists+xml", "rl", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/resource-lists-diff+xml", "rld", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/rls-services+xml", "rs", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/rsd+xml", "rsd", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/rss+xml", "rss", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/shf+xml", "shf", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/smil+xml", "smi", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/smil+xml", "smil", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/sparql-query", "rq", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/sparql-results+xml", "srx", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/srgs+xml", "grxml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/ssml+xml", "ssml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/vnd.chemdraw+xml", "cdxml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/vnd.criticaltools.wbs+xml", "wbs", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-scheme", "scm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("image/svg+xml", "svg", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("image/vnd.dxf", "dxf", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("image/x-xpixmap", "xpm", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("message/rfc822", "eml", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("message/rfc822", "mime", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-patch", "dif", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-patch", "diff", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/x-patch", "patch", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "tf", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "tfvar", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("text/plain", "tfvars", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/json", "libsonnet", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/json", "jsonnet", ExtensionInfo.ExtType.TEXT));
        add(new ExtensionInfo("application/x-typescript", "tsx", ExtensionInfo.ExtType.TEXT));
    }

    public static void add(ExtensionInfo ez) {
        map.put(ez.getFileExtension(), ez);
    }

    public static ExtensionInfo.ExtType getExtensionType(String file) {
        String ext = FilenameUtils.getExtension(file);
        if (ext != null) {
            ext = ext.toLowerCase(java.util.Locale.ROOT);
        } else {
            ext = "";
        }
        return map.getOrDefault(ext, new ExtensionInfo("","", ExtensionInfo.ExtType.UNKNOWN)).getExtType();
    }

    public static boolean isImage(String file) {
        return getExtensionType(file) == ExtensionInfo.ExtType.IMAGE;
    }

    public static boolean isText(String file) {
        return getExtensionType(file) == ExtensionInfo.ExtType.TEXT;
    }

}
