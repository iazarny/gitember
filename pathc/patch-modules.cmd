
jdeps --ignore-missing-deps --generate-module-info . C:/Users/Igor_Azarny/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar
javac --patch-module org.apache.commons.lang3=C:/Users/Igor_Azarny/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar org.apache.commons.lang3\module-info.java
jar uf C:/Users/Igor_Azarny/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar -C org.apache.commons.lang3 module-info.class

jdeps --ignore-missing-deps --generate-module-info . C:\Users\Igor_Azarny\.m2\repository\org\eclipse\jgit\org.eclipse.jgit\5.3.9.202012012026-r\org.eclipse.jgit-5.3.9.202012012026-r.jar
javac --patch-module org.eclipse.jgit=C:\Users\Igor_Azarny\.m2\repository\org\eclipse\jgit\org.eclipse.jgit\5.3.9.202012012026-r\org.eclipse.jgit-5.3.9.202012012026-r.jar org.eclipse.jgit\module-info.java
jar uf C:\Users\Igor_Azarny\.m2\repository\org\eclipse\jgit\org.eclipse.jgit\5.3.9.202012012026-r\org.eclipse.jgit-5.3.9.202012012026-r.jar -C org.eclipse.jgit module-info.class

jdeps --ignore-missing-deps --generate-module-info . C:\Users\Igor_Azarny\.m2\repository\commons-io\commons-io\2.8.0\commons-io-2.8.0.jar
javac --patch-module org.apache.commons.io=C:\Users\Igor_Azarny\.m2\repository\commons-io\commons-io\2.8.0\commons-io-2.8.0.jar org.apache.commons.io\module-info.java
jar uf C:\Users\Igor_Azarny\.m2\repository\commons-io\commons-io\2.8.0\commons-io-2.8.0.jar -C org.apache.commons.io module-info.class


jdeps --ignore-missing-deps --generate-module-info . C:\Users\Igor_Azarny\.m2\repository\com\jcraft\jsch\0.1.55\jsch-0.1.55.jar
javac --patch-module jsch=C:\Users\Igor_Azarny\.m2\repository\com\jcraft\jsch\0.1.55\jsch-0.1.55.jar jsch\module-info.java
jar uf C:\Users\Igor_Azarny\.m2\repository\com\jcraft\jsch\0.1.55\jsch-0.1.55.jar -C jsch module-info.class
