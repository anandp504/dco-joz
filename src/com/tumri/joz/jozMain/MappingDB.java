// The mapping database.

package com.tumri.joz.jozMain;

import java.util.List;

public interface MappingDB
{
    public List<MappingObj> get_realm_t_specs (String realm);

    public List<MappingObj> get_theme_t_specs (String theme);

    public List<MappingObj> get_store_id_t_specs (String store_id);
}
