package com.az.gitember.service.detector.impl;

import com.az.gitember.service.detector.Confidence;

import java.util.regex.Pattern;

/**
 * Created by Igor Azarny iazarny@yahoo.com on March 21  2026.
 */
public record PatternRule(
        Pattern pattern,
        String description,
        Confidence confidence
) {}
