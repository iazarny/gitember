package com.az.gitember.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Golden-key guard for {@link Project}: with {@code fieldVisibility = ANY}, Jackson serializes
 * every non-{@code @JsonIgnore} field. A forgotten {@code @JsonIgnore} on one of the runtime
 * fields (a {@link Thread}, a plain {@code Object} lock, a {@code ConcurrentHashMap}, ...) would
 * either fail the write outright or silently bloat/corrupt {@code ~/.gitember/gitember2.json}
 * for every user. This test fails loudly instead.
 */
class ProjectJsonRoundTripTest {

    private static final Set<String> EXPECTED_KEYS = Set.of(
            "projectHomeFolder", "openTime", "userName", "empId", "userPwd", "userKey", "keyPass",
            "accessToken", "userCommitName", "userCommitEmail", "committerName", "committerEmail",
            "indexed", "showAllPullRequests"
    );

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void serialization_touchesOnlyThePersistedFields() throws Exception {
        Project p = new Project("C:\\dev\\p", new Date());
        p.setUserName("alice");
        p.setUserPwd("secret");
        p.setAccessToken("tok");
        p.setIndexed(true);
        p.setShowAllPullRequests(true);

        // Also populate runtime-only state, so a missing @JsonIgnore on any of it would show up.
        p.setWorkingBranch(null);
        p.isLfsRepo();
        p.getLocalBranches();
        p.getScmRevisionInformationCache().size();

        String json = MAPPER.writeValueAsString(p);
        JsonNode node = MAPPER.readTree(json);

        Set<String> actualKeys = new HashSet<>();
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            actualKeys.add(it.next());
        }

        assertEquals(EXPECTED_KEYS, actualKeys,
                "Project must serialize exactly its persisted fields -- a forgotten @JsonIgnore "
                        + "on a runtime field would leak into every user's settings file");
    }

    @Test
    void maskedFields_roundTrip_forVariousValues() throws Exception {
        for (String value : new String[] {"", "tökén-non-ascii-é", "x".repeat(200)}) {
            Project p = new Project("C:\\dev\\p", new Date());
            p.setAccessToken(value);
            p.setUserPwd(value);
            p.setKeyPass(value);

            String json = MAPPER.writeValueAsString(p);
            Project back = MAPPER.readValue(json, Project.class);

            assertEquals(value, back.getAccessToken());
            assertEquals(value, back.getUserPwd());
            assertEquals(value, back.getKeyPass());
        }
    }

    @Test
    void settingsRoundTrip_containsNoInterningRegistryKey() throws Exception {
        Settings settings = new Settings();
        settings.getProjects().add(new Project("C:\\dev\\p", new Date()));
        settings.internAll();

        String json = MAPPER.writeValueAsString(settings);
        JsonNode node = MAPPER.readTree(json);

        assertFalse(node.has("byKey"), "the transient interning registry must never be serialized");
    }
}
