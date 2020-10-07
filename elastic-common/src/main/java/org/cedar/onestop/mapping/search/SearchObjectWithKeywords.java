package org.cedar.onestop.mapping.search;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
// import org.cedar.onestop.mapping.Link;

public interface SearchObjectWithKeywords {

    public Set<String> getKeywords();
    public void setKeywords(Set<String> keywords);
    public SearchObjectWithKeywords withKeywords(Set<String> keywords);

    public Set<String> getGcmdScience();
    public void setGcmdScience(Set<String> gcmdScience);
    public SearchObjectWithKeywords withGcmdScience(Set<String> gcmdScience);

    public Set<String> getGcmdScienceServices();
    public void setGcmdScienceServices(Set<String> gcmdScienceServices);
    public SearchObjectWithKeywords withGcmdScienceServices(Set<String> gcmdScienceServices);

    public Set<String> getGcmdLocations();
    public void setGcmdLocations(Set<String> gcmdLocations);
    public SearchObjectWithKeywords withGcmdLocations(Set<String> gcmdLocations);

    public Set<String> getGcmdInstruments();
    public void setGcmdInstruments(Set<String> gcmdInstruments);
    public SearchObjectWithKeywords withGcmdInstruments(Set<String> gcmdInstruments);

    public Set<String> getGcmdPlatforms();
    public void setGcmdPlatforms(Set<String> gcmdPlatforms);
    public SearchObjectWithKeywords withGcmdPlatforms(Set<String> gcmdPlatforms);

    public Set<String> getGcmdProjects();
    public void setGcmdProjects(Set<String> gcmdProjects);
    public SearchObjectWithKeywords withGcmdProjects(Set<String> gcmdProjects);

    public Set<String> getGcmdDataCenters();
    public void setGcmdDataCenters(Set<String> gcmdDataCenters);
    public SearchObjectWithKeywords withGcmdDataCenters(Set<String> gcmdDataCenters);

    public Set<String> getGcmdHorizontalResolution();
    public void setGcmdHorizontalResolution(Set<String> gcmdHorizontalResolution);
    public SearchObjectWithKeywords withGcmdHorizontalResolution(Set<String> gcmdHorizontalResolution);

    public Set<String> getGcmdVerticalResolution();
    public void setGcmdVerticalResolution(Set<String> gcmdVerticalResolution);
    public SearchObjectWithKeywords withGcmdVerticalResolution(Set<String> gcmdVerticalResolution);

    public Set<String> getGcmdTemporalResolution();
    public void setGcmdTemporalResolution(Set<String> gcmdTemporalResolution);
    public SearchObjectWithKeywords withGcmdTemporalResolution(Set<String> gcmdTemporalResolution) ;



}
