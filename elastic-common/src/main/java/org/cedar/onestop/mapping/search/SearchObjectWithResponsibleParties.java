package org.cedar.onestop.mapping.search;

import java.util.Set;

public interface SearchObjectWithResponsibleParties {

    public Set<String> getIndividualNames();
    public void setIndividualNames(Set<String> individualNames);
    public SearchObjectWithResponsibleParties withIndividualNames(Set<String> individualNames);

    public Set<String> getOrganizationNames();
    public void setOrganizationNames(Set<String> organizationNames);
    public SearchObjectWithResponsibleParties withOrganizationNames(Set<String> organizationNames);
}
