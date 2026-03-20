package com.az.gitember.service.detector;

import java.util.regex.Pattern;

public record PatternRule(
        Pattern pattern,
        String description,
        Confidence confidence
) {}
