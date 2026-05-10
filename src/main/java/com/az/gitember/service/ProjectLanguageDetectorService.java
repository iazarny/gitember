package com.az.gitember.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Service that scans project root directory,
 * detects build/dependency/project descriptor files
 * and returns probable programming languages.
 *
 * Detection rules are configurable.
 */

public class ProjectLanguageDetectorService {

    /**
     * file name -> language mapping
     */
    private final Map<String, String> fileLanguageMap;

    public ProjectLanguageDetectorService(Map<String, String> fileLanguageMap) {
        this.fileLanguageMap = new HashMap<>(fileLanguageMap);
    }

    /**
     * Detect languages in project.
     *
     * @param root project root
     * @return detected languages
     */
    public Set<String> detectLanguages(Path root) throws IOException {

        Set<String> detectedLanguages = new HashSet<>();

        try (Stream<Path> stream = Files.walk(root)) {

            stream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .forEach(fileName -> {

                        String language = fileLanguageMap.get(fileName);

                        if (language != null) {
                            detectedLanguages.add(language);
                        }

                        // support wildcard rules like *.csproj
                        fileLanguageMap.forEach((pattern, lang) -> {
                            if (matches(pattern, fileName)) {
                                detectedLanguages.add(lang);
                            }
                        });
                    });
        }

        return detectedLanguages;
    }

    private boolean matches(String pattern, String fileName) {

        if (!pattern.contains("*")) {
            return pattern.equals(fileName);
        }

        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*");

        return fileName.matches(regex);
    }

    /**
     * Default known mappings.
     */
    public static Map<String, String> defaultMappings() {

        Map<String, String> map = new HashMap<>();

        // Java / JVM
        map.put("pom.xml", "Java");
        map.put("build.gradle", "Java");
        map.put("build.gradle.kts", "Kotlin");
        map.put("settings.gradle", "Java");
        map.put("settings.gradle.kts", "Kotlin");
        map.put("build.sbt", "Scala");

        // Python
        map.put("pyproject.toml", "Python");
        map.put("requirements.txt", "Python");
        map.put("setup.py", "Python");
        map.put("Pipfile", "Python");

        // JS / TS
        map.put("package.json", "JavaScript");
        map.put("tsconfig.json", "TypeScript");

        // Rust
        map.put("Cargo.toml", "Rust");

        // Go
        map.put("go.mod", "Go");

        // PHP
        map.put("composer.json", "PHP");

        // Ruby
        map.put("Gemfile", "Ruby");

        // Dart / Flutter
        map.put("pubspec.yaml", "Dart");

        // .NET
        map.put("*.csproj", "C#");
        map.put("*.fsproj", "F#");

        // C/C++
        map.put("CMakeLists.txt", "C++");
        map.put("Makefile", "C/C++");

        // Swift
        map.put("Package.swift", "Swift");

        // Terraform
        map.put("main.tf", "Terraform");

        return map;
    }


}
