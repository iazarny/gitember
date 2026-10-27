package com.az.gitember.service.tracker;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueKeyDetectorTest {

    @Test
    void findFirst_fromCommitMessage() {
        assertEquals("PAY-123", IssueKeyDetector.findFirst("PAY-123 Fix payment timeout"));
        assertEquals("CORE-9", IssueKeyDetector.findFirst("Refs: feature/CORE-9-login"));
        assertNull(IssueKeyDetector.findFirst("no ticket here"));
        assertNull(IssueKeyDetector.findFirst(null));
        assertNull(IssueKeyDetector.findFirst(""));
    }

    @Test
    void findAll_deduplicates() {
        List<String> keys = IssueKeyDetector.findAll("PAY-123 and CORE-1 then PAY-123 again");
        assertEquals(List.of("PAY-123", "CORE-1"), keys);
    }

    @Test
    void contains_key() {
        assertTrue(IssueKeyDetector.contains("PAY-123 Fix timeout", "PAY-123"));
        assertFalse(IssueKeyDetector.contains("Fix timeout", "PAY-123"));
        assertFalse(IssueKeyDetector.contains(null, "PAY-123"));
    }
}
