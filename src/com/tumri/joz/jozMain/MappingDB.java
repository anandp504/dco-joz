// The mapping database.

package com.tumri.joz.jozMain;

import java.util.List;

public interface MappingDB
{
    public MappingObjList get_url_t_specs (String url);
    public MappingObjList get_url_t_specs (JozURI uri);

    public MappingObjList get_theme_t_specs (String theme);

    public MappingObjList get_store_id_t_specs (String store_id);

    public void add (String kind, String data, String tspec_name,
		     Float weight, Long mod_time);

    public void delete (String kind, String data, String tspec_name,
			/* weight is ignored */
			Long mod_time);
}
