// An entry in the MUP product database.
// NOTE: There are no setter methods on purpose!
// NOTE: null means "unspecified".
// FIXME: wip

package com.tumri.joz.jozMain;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.tumri.zini.transport.FASLType;

import com.tumri.utils.ifasl.IFASLUtils;

import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.IProduct;

public class MUPProductObj
{
    MUPProductObj (IProduct ip)
    {
	DictionaryManager dm = DictionaryManager.getInstance ();
	// FIXME: The casts here are unfortunate.
	_guid = ip.getGId ();
	_product_id = ip.getGId ();
	_catalog = (String) dm.getValue (IProduct.Attribute.kCatalog, ip.getCatalog ());
	_parents = new ArrayList<String> ();
	_parents.add ((String) dm.getValue (IProduct.Attribute.kCategory, ip.getCategory ()));
	_price = ip.getPrice ();
	_discount_price = ip.getDiscountPrice ();
	_brand = (String) dm.getValue (IProduct.Attribute.kBrand, ip.getBrand ());
	_retailer = (String) dm.getValue (IProduct.Attribute.kSupplier, ip.getSupplier ());
	_merchant = (String) dm.getValue (IProduct.Attribute.kProvider, ip.getProvider ());
	_name = ip.getProductName ();
	_description = ip.getDescription ();
	_rank = ip.getRank ();
	_thumbnail_url = ip.getThumbnail ();
	_purchase_url = ip.getPurchaseUrl ();
	_image_url = ip.getImageUrl ();
	_image_width = ip.getImageWidth ();
	_image_height = ip.getImageHeight ();
	_cpc = ip.getCPC ();
	_currency = (String) dm.getValue (IProduct.Attribute.kCurrency, ip.getCurrency ());
	_discount_currency = (String) dm.getValue (IProduct.Attribute.kDiscountPriceCurrency, ip.getDiscountPriceCurrency ());
	_black_white_list_status = ip.getBlackWhiteListStatus ();
	_product_type = (String) dm.getValue (IProduct.Attribute.kProductType, ip.getProductType ());
	_cpo_cpa = ip.getCPO ();
	_base_product_number = ip.getBaseProductNumber ();
    }

    public MUPProductObj (FASLType t, Iterator<FASLType> iter)
	throws BadMUPDataException
    {
	// Assign this separately so we have the guid if we throw an
	// exception for the rest of the stuff.
	try
	{
	    _guid = IFASLUtils.toString (iter.next (), "guid");
	}
	catch (Exception e)
	{
	    throw new BadMUPDataException ("parsing guid");
	}

	try
	{
	    _catalog = IFASLUtils.toString (iter.next (), "catalog");
	    _product_id = IFASLUtils.toString (iter.next (), "product id");
	    _parents = new ArrayList<String> ();
	    _parents.add (IFASLUtils.toString (iter.next (), "parents"));
	    _price = IFASLUtils.toDouble (iter.next (), "price");
	    _discount_price = IFASLUtils.toDouble (iter.next (), "discount price");
	    _brand = IFASLUtils.toString (iter.next (), "brand");
	    _retailer = IFASLUtils.toString (iter.next (), "retailer");
	    _merchant = IFASLUtils.toString (iter.next (), "merchant");
	    _name = IFASLUtils.toString (iter.next (), "name");
	    _description = IFASLUtils.toString (iter.next (), "description");
	    _rank = IFASLUtils.toInteger (iter.next (), "rank");
	    _thumbnail_url = IFASLUtils.toString (iter.next (), "thumbnail url");
	    _purchase_url = IFASLUtils.toString (iter.next (), "purchase url");
	    _image_url = IFASLUtils.toString (iter.next (), "image url");
	    _image_width = IFASLUtils.toInteger (iter.next (), "image width");
	    _image_height = IFASLUtils.toInteger (iter.next (), "image height");
	    _cpc = IFASLUtils.toDouble (iter.next (), "cpc");
	    _currency = IFASLUtils.toString (iter.next (), "currency");
	    _discount_currency = IFASLUtils.toString (iter.next (), "discount currency");
	    _black_white_list_status = IFASLUtils.toInteger (iter.next (), "black white list status");
	    _product_type = IFASLUtils.toString (iter.next (), "product type");
	    _cpo_cpa = IFASLUtils.toDouble (iter.next (), "cpo/cpa");
	    _base_product_number = IFASLUtils.toString (iter.next (), "base product number");
	    // soz reads "long-description" here, but ignores it
	}
	catch (Exception e)
	{
	    throw new BadMUPDataException (_guid.toString () + ": " + e.getMessage ());
	}
    }

    public String get_guid () { return _guid; }

    public String get_product_id () { return _product_id; }

    public String get_catalog () { return _catalog; }

    public List<String> get_parents () { return _parents; }

    public Double get_price () { return _price; }

    public Double get_discount_price () { return _discount_price; }

    public String get_brand () { return _brand; }

    public String get_retailer () { return _retailer; }

    public String get_merchant () { return _merchant; }

    public String get_name () { return _name; }

    public String get_description () { return _description; }

    public Integer get_rank () { return _rank; }

    public String get_thumbnail_url () { return _thumbnail_url; }

    public String get_purchase_url () { return _purchase_url; }

    public String get_image_url () { return _image_url; }

    public Integer get_image_width () { return _image_width; }

    public Integer get_image_height () { return _image_height; }

    public Double get_cpc () { return _cpc; }

    public String get_currency () { return _currency; }

    public String get_discount_currency () { return _discount_currency; }

    public Integer get_black_white_list_status () { return _black_white_list_status; }

    public String get_product_type () { return _product_type; }

    public Double get_cpo_cpa () { return _cpo_cpa; }

    public String get_base_product_number () { return _base_product_number; }

    //-------------------------------------------------------------------------

    private String _guid;

    private String _product_id;

    private String _catalog;

    List<String> _parents;

    private Double _price;

    private Double _discount_price;

    private String _brand;

    private String _retailer;

    private String _merchant;

    private String _name;

    private String _description;

    private Integer _rank;

    private String _thumbnail_url;

    private String _purchase_url;

    private String _image_url;

    private Integer _image_width;

    private Integer _image_height;

    private Double _cpc;

    private String _currency;

    private String _discount_currency;

    private Integer _black_white_list_status;

    private String _product_type;

    private Double _cpo_cpa;

    private String _base_product_number;
}
