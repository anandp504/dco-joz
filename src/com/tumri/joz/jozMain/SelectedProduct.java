// Container for a selected product.

package com.tumri.joz.jozMain;

public class SelectedProduct
{
    public SelectedProduct (MUPProductObj prod /*FIXME: wip*/)
    {
	_product = prod;
    }

    // Return a string suitable for passing back to the client.
    // See soz-product-selector.lisp:morph-product-list-into-sexpr-js-friendly.

    public String
    toAdDataResultString ()
    {
	StringBuilder b = new StringBuilder ();

	b.append ("{");

	// FIXME: wip

	b.append ("id:");
	b.append (",display_category_name:");
	b.append (",price:");
	b.append (",discount_price:");
	b.append (",brand:");
	b.append (",merchant_id:");
	b.append (",provider:");
	b.append (",merchantlogo:");
	b.append (",ship_promo:");
	b.append (",description:");
	b.append (",thumbnailraw:");
	b.append (",product_url:");
	b.append (",picture_url:");
	b.append (",c_code:");
	b.append (",offer_type:");

	b.append ("}");

	return b.toString ();
    }

    // implementation details -------------------------------------------------

    MUPProductObj _product;
}
