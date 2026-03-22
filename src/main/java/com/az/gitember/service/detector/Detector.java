package com.az.gitember.service.detector;

import java.util.List;

/**
 * Represent source code scan finding.
 * Created by Igor Azarny iazarny@yahoo.com on March 21  2026.
 */
public interface Detector {

    List<Finding> detect(ScanContext context);

    String name();

}