package com.tumri.joz.campaign;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.misc.SexpOSpecHelper;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpSymbol;

/**
 * Class to handle the t-spec-add, t-spec-delete and incorp-mapping-delta API
 * @author nipun
 *
 */
public class OSpecHelper {

	private static Logger log = Logger.getLogger (OSpecHelper.class);
	
	/**
	 * Parses the tspec-add directive and adds an Ospec to the cache.
	 * This is used when creating a tSpec from the consoles. This tSpec does not become part of the Campaign Cache
	 * @param tSpecAddSpec - the string expression that contains the tspec add command
	 */
	public static void doTSpecAdd(SexpList tSpecAddSpec) {
		SexpList l = tSpecAddSpec;
		Sexp cmd_expr = l.getFirst ();
		if (! cmd_expr.isSexpSymbol ())
			log.error("command name not a symbol: " + cmd_expr.toString ());

		SexpSymbol sym = cmd_expr.toSexpSymbol ();
		String cmd_name = sym.toString ();
		if (cmd_name.equalsIgnoreCase("t-spec-add")) {
			Iterator<Sexp> iter = l.iterator();
			iter.next(); //ignore the t-spec-add keyword
			OSpec theOSpec = SexpOSpecHelper.readTSpecDetailsFromSExp(iter);
			CampaignDB.getInstance().addOSpec(theOSpec);
			//Note that we are not touching the Query cache here since the next access to the Tspec using get-ad-data will add it to the cache
		} else {
			log.error("Unexpected command received : " + cmd_expr);
		}
	}

	/**
	 * Deletes the reference of the TSpec from JoZ cache
	 * @param tSpecName - name of the tspec to delete
	 */
	public static void doTSpecDelete(String tSpecName) {
	    CampaignDB.getInstance().deleteOSpec(tSpecName);
	    OSpecQueryCache.getInstance().removeQuery(tSpecName);
	}

	/**
	 * Update the TSpec mapping on the fly
	 * @param updtMappingCommands list of mapping commands
	 */
	public static void doUpdateTSpecMapping(SexpList updtMappingCommands){
	    //TODO: Finalize what needs to be done for updating the mappings after looking at the usecases for TMC/QAC/Publisher/Advertiser
		for (Sexp cmd : updtMappingCommands) {
		if (! cmd.isSexpList ()) {
		    log.error("incorp-mapping-deltas command is not a list: " + cmd.toString());
		    continue;
		}
		SexpList updtMappingCommand = cmd.toSexpList();
		if (updtMappingCommand.size() != 6) {
		    log.error("Invalid syntax for the incorp-mapping-deltas command: " + updtMappingCommand.toString());
		    continue;
		}
		Sexp tmpOpType = updtMappingCommand.get(0);
		Sexp tmpLookupDataType = updtMappingCommand.get(1);
		Sexp tmpRealmStoreIdVal = updtMappingCommand.get(2);
		Sexp tmpTSpec = updtMappingCommand.get(3);
		Sexp tmpweight = updtMappingCommand.get(4);
		Sexp tmpmodified = updtMappingCommand.get(5);

		// FIXME: more error checking is required here
		String opType = tmpOpType.toStringValue();
		String opLookupDataType = tmpLookupDataType.toStringValue();
		String value = tmpRealmStoreIdVal.toStringValue();
		String tSpecName = tmpTSpec.toStringValue();
		float weight = 0;
		if (tmpweight.isSexpReal())
		    weight = new Float(tmpweight.toSexpReal().toNativeReal64()).floatValue();
		else
		    weight = (float) tmpweight.toSexpInteger().toNativeInteger32();
		String modTime = tmpmodified.toStringValue();
		OSpec aOSpec = CampaignDB.getInstance().getOspec(tSpecName);
		if (aOSpec != null) {
		    if (":add".equals(opType)) {
			if (aOSpec == null) {
			    throw new RuntimeException("Could not locate the oSpec in the cache using name : " + tSpecName);
			} else {
			    if (":realm".equals(opLookupDataType)) {
				//Add the realm mapping to the TSpec
			    } else if (":store-ID".endsWith(opLookupDataType)) {
			    }
			}
		    } else if (":delete".equals(opType)) {
			if (":realm".equals(opLookupDataType)) {
			    //Delete the realm mapping to the TSpec
			} else if (":store-ID".endsWith(opLookupDataType)) {
			    //Delete the storeid mapping to the TSpec
			}
		    }
		}
	    }
	}

}
