// Container for a selected product.
// TODO: OMG, OMG, this is all wrong.  Chill dude.  As things evolve this
// will get rewritten to be The Right Way.

package com.tumri.joz.jozMain;

import java.util.List;

import com.tumri.utils.strings.EString;

public class SelectedProduct
{
    public SelectedProduct (MUPProductObj prod /*FIXME: wip*/)
    {
	_product = prod;
    }

    // Return a string suitable for passing back to the client.
    // See soz-product-selector.lisp:morph-product-list-into-sexpr-js-friendly.
    //
    // NOTE: If there is any RFC1630-like encoding that is needed, do it here.
    // Not everything needs to be encoded, and some things may need to be
    // encoded differently.

    public String
    toAdDataResultString ()
    {
	StringBuilder b = new StringBuilder ();

	b.append ("{");

	// FIXME: wip

	b.append ("id:\"");
	b.append (encode (_product.get_product_id ()));
	b.append ("\",display_category_name:\"");
	// Use the first parent as the category.
	b.append (encode (_product.get_parents ().get (0)));
	b.append ("\",price:\"");
	b.append (encode_price (_product.get_price ()));
	b.append ("\",discount_price:\"");
	b.append (encode_price (_product.get_discount_price ()));
	b.append ("\",brand:\"");
	b.append (encode (_product.get_brand ()));
	b.append ("\",merchant_id:\"");
	b.append (encode (_product.get_merchant ()));
	b.append ("\",provider:\"");
	b.append (encode (_product.get_retailer ()));
	b.append ("\",merchantlogo:\"");
	b.append (encode (JozData.merchant_db.get_logo_url (_product.get_merchant ())));
	b.append ("\",ship_promo:\"");
	b.append (encode (JozData.merchant_db.get_shipping_promo (_product.get_merchant ())));
	b.append ("\",description:\"");
	// FIXME: soz has code to limit size of description passed back
	b.append (encode (_product.get_description ()));
	b.append ("\",thumbnailraw:\"");
	b.append (encode (_product.get_thumbnail_url ()));
	b.append ("\",product_url:\"");
	b.append (encode (_product.get_purchase_url ()));
	b.append ("\",picture_url:\"");
	b.append (encode (_product.get_image_url ()));
	b.append ("\",c_code:\"");
	b.append (encode (_product.get_currency ()));
	b.append ("\",offer_type:\"");
	b.append (encode (_product.get_product_type ()));
	b.append ("\"");

	b.append ("}");

	return b.toString ();
    }

    // Return the selected product's id.

    public String
    get_product_id ()
    {
	return _product.get_product_id ();
    }

    // Return the selected product's parents (categories).

    public List<String>
    get_parents ()
    {
	return _product.get_parents ();
    }

    // Return the product's brand or null if there is none.

    public String
    get_brand ()
    {
	return _product.get_brand ();
    }

    // Return the product's merchant or null if there is none.

    public String
    get_merchant ()
    {
	return _product.get_merchant ();
    }

    // implementation details -------------------------------------------------

    MUPProductObj _product;

    private String
    encode (EString es)
    {
	// FIXME: wip
	if (es == null)
	    return "nil";
	return es.toString ();
    }

    private String
    encode (String s)
    {
	// FIXME: wip
	if (s == null)
	    return "nil";
	return s;
    }

    private String
    encode_price (Float f)
    {
	// FIXME: wip
	if (f == null)
	    return "nil";
	return String.format ("%.2f", f);
    }

    private String
    encode_price (Double d)
    {
	// FIXME: wip
	if (d == null)
	    return "nil";
	return String.format ("%.2f", d);
    }
}
