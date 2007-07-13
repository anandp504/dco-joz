// An entry in the MUP product database.
// NOTE: There are no setter methods on purpose!
// NOTE: null means "unspecified".
// FIXME: wip

package com.tumri.joz.jozMain;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.tumri.zini.transport.FASLType;

import com.tumri.utils.strings.EString;
import com.tumri.utils.strings.ProductName;
import com.tumri.utils.ifasl.IFASLUtils;

public class MUPProductObj
{
    MUPProductObj (
	ProductName guid,
	EString product_id,
	EString catalog,
	List<EString> parents,
	Float price,
	Float discount_price,
	EString brand,
	EString retailer,
	EString merchant,
	EString name,
	EString description,
	Integer rank,
	EString thumbnail_url,
	EString purchase_url,
	EString image_url,
	Integer image_width,
	Integer image_height,
	Integer cpc,
	EString currency,
	EString discount_currency,
	Integer black_white_list_status,
	EString product_type,
	Float cpo_cpa,
	EString base_product_number
	)
    {
	_guid = guid;
	_product_id = product_id;
	_catalog = catalog;
	_parents = parents;
	_price = price;
	_discount_price = discount_price;
	_brand = brand;
	_retailer = retailer;
	_merchant = merchant;
	_name = name;
	_description = description;
	_rank = rank;
	_thumbnail_url = thumbnail_url;
	_image_url = image_url;
	_image_width = image_width;
	_image_height = image_height;
	_cpc = cpc;
	_currency = currency;
	_discount_currency = discount_currency;
	_black_white_list_status = black_white_list_status;
	_product_type = product_type;
	_cpo_cpa = cpo_cpa;
	_base_product_number = base_product_number;
    }

    public MUPProductObj (FASLType t, Iterator<FASLType> iter)
	throws BadMUPDataException
    {
	// Assign this separately so we have the guid if we throw an
	// exception for the rest of the stuff.
	try
	{
	    _guid = IFASLUtils.toProductName (iter.next (), "guid");
	}
	catch (Exception e)
	{
	    throw new BadMUPDataException ("parsing guid");
	}

	try
	{
	    _catalog = IFASLUtils.toEString (iter.next (), "catalog");
	    _product_id = IFASLUtils.toEString (iter.next (), "product id");
	    _parents = new ArrayList<EString> ();
	    _parents.add (IFASLUtils.toEString (iter.next (), "parents"));
	    _price = IFASLUtils.toFloat (iter.next (), "price");
	    _discount_price = IFASLUtils.toFloat (iter.next (), "discount price");
	    _brand = IFASLUtils.toEString (iter.next (), "brand");
	    _retailer = IFASLUtils.toEString (iter.next (), "retailer");
	    _merchant = IFASLUtils.toEString (iter.next (), "merchant");
	    _name = IFASLUtils.toEString (iter.next (), "name");
	    _description = IFASLUtils.toEString (iter.next (), "description");
	    _rank = IFASLUtils.toInteger (iter.next (), "rank");
	    _thumbnail_url = IFASLUtils.toEString (iter.next (), "thumbnail url");
	    _purchase_url = IFASLUtils.toEString (iter.next (), "purchase url");
	    _image_url = IFASLUtils.toEString (iter.next (), "image url");
	    _image_width = IFASLUtils.toInteger (iter.next (), "image width");
	    _image_height = IFASLUtils.toInteger (iter.next (), "image height");
	    _cpc = IFASLUtils.toInteger (iter.next (), "cpc");
	    _currency = IFASLUtils.toEString (iter.next (), "currency");
	    _discount_currency = IFASLUtils.toEString (iter.next (), "discount currency");
	    _black_white_list_status = IFASLUtils.toInteger (iter.next (), "black white list status");
	    _product_type = IFASLUtils.toEString (iter.next (), "product type");
	    _cpo_cpa = IFASLUtils.toFloat (iter.next (), "cpo/cpa");
	    _base_product_number = IFASLUtils.toEString (iter.next (), "base product number");
	    // soz reads "long-description" here, but ignores it
	}
	catch (Exception e)
	{
	    throw new BadMUPDataException (_guid.toString () + ": " + e.getMessage ());
	}
    }

    public ProductName get_guid () { return _guid; }

    public EString get_product_id () { return _product_id; }

    public EString get_catalog () { return _catalog; }

    public List<EString> get_parents () { return _parents; }

    public Float get_price () { return _price; }

    public Float get_discount_price () { return _discount_price; }

    public EString get_brand () { return _brand; }

    public EString get_retailer () { return _retailer; }

    public EString get_merchant () { return _merchant; }

    public EString get_name () { return _name; }

    public EString get_description () { return _description; }

    public Integer get_rank () { return _rank; }

    public EString get_thumbnail_url () { return _thumbnail_url; }

    public EString get_purchase_url () { return _purchase_url; }

    public EString get_image_url () { return _image_url; }

    public Integer get_image_width () { return _image_width; }

    public Integer get_image_height () { return _image_height; }

    public Integer get_cpc () { return _cpc; }

    public EString get_currency () { return _currency; }

    public EString get_discount_currency () { return _discount_currency; }

    public Integer get_black_white_list_status () { return _black_white_list_status; }

    public EString get_product_type () { return _product_type; }

    public Float get_cpo_cpa () { return _cpo_cpa; }

    public EString get_base_product_number () { return _base_product_number; }

    //-------------------------------------------------------------------------

    private ProductName _guid;

    private EString _product_id;

    private EString _catalog;

    List<EString> _parents;

    private Float _price;

    private Float _discount_price;

    private EString _brand;

    private EString _retailer;

    private EString _merchant;

    private EString _name;

    private EString _description;

    private Integer _rank;

    private EString _thumbnail_url;

    private EString _purchase_url;

    private EString _image_url;

    private Integer _image_width;

    private Integer _image_height;

    private Integer _cpc;

    private EString _currency;

    private EString _discount_currency;

    private Integer _black_white_list_status;

    private EString _product_type;

    private Float _cpo_cpa;

    private EString _base_product_number;
}
