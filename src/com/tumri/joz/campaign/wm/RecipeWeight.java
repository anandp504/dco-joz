/*
 * RecipeWeight.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.campaign.wm;

/**
 * Recipe Weight Impl
 * @author: nipun
 * Date: Aug 3, 2009
 * Time: 2:05:23 PM
 */
public class RecipeWeight {
    private int recipeId;
    private float weight;

    public RecipeWeight(Integer recipeId, float weight) {
        this.recipeId = recipeId;
        this.weight = weight;
    }
    
    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
