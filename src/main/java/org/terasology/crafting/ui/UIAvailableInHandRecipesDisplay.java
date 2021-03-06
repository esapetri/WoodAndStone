/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.ui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.terasology.crafting.event.UserCraftInHandRequest;
import org.terasology.crafting.system.CraftInHandRecipeRegistry;
import org.terasology.crafting.system.recipe.CraftInHandRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.UIDisplayElement;

import javax.vecmath.Vector2f;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UIAvailableInHandRecipesDisplay extends UIDisplayContainerScrollable {
    private Multimap<String, String> displayedRecipes = HashMultimap.create();
    private CraftInHandRecipeRegistry registry;
    private EntityRef character;

    public UIAvailableInHandRecipesDisplay(Vector2f size, CraftInHandRecipeRegistry registry, EntityRef character) {
        super(size);
        this.registry = registry;
        this.character = character;
        loadRecipes();
    }

    public void update() {
        // TODO: Naive approach by comparing all the possible recipes to those currently displayed
        Multimap<String, String> recipes = HashMultimap.create();
        for (Map.Entry<String, CraftInHandRecipe> craftInHandRecipe : registry.getRecipes().entrySet()) {
            String recipeId = craftInHandRecipe.getKey();
            List<CraftInHandRecipe.CraftInHandResult> results = craftInHandRecipe.getValue().getMatchingRecipeResults(character);
            if (results != null) {
                for (CraftInHandRecipe.CraftInHandResult result : results) {
                    String resultId = result.getResultId();
                    recipes.put(recipeId, resultId);
                }
            }
        }

        if (!recipes.equals(displayedRecipes)) {
            reloadRecipes();
        }

        super.update();
    }

    private void reloadRecipes() {
        List<UIDisplayElement> uiDisplayElements = new LinkedList<>(getDisplayElements());
        for (UIDisplayElement uiDisplayElement : uiDisplayElements) {
            if (uiDisplayElement instanceof UIRecipeDisplay) {
                UIRecipeDisplay recipeDisplay = (UIRecipeDisplay) uiDisplayElement;
                removeDisplayElement(recipeDisplay);
                recipeDisplay.dispose();
            }
        }

        loadRecipes();
    }

    public void loadRecipes() {
        int rowHeight = 50;
        int rowIndex = 0;

        displayedRecipes.clear();
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        for (Map.Entry<String, CraftInHandRecipe> craftInHandRecipe : registry.getRecipes().entrySet()) {
            final String recipeId = craftInHandRecipe.getKey();
            List<CraftInHandRecipe.CraftInHandResult> results = craftInHandRecipe.getValue().getMatchingRecipeResults(character);
            if (results != null) {
                for (CraftInHandRecipe.CraftInHandResult result : results) {
                    final String resultId = result.getResultId();
                    displayedRecipes.put(recipeId, resultId);
                    UIRecipeDisplay recipeDisplay = new UIRecipeDisplay(recipeId, resultId, inventoryManager, character, result,
                            new CreationCallback() {
                                @Override
                                public void createOne() {
                                    character.send(new UserCraftInHandRequest(recipeId, resultId));
                                }
                            });
                    recipeDisplay.setPosition(new Vector2f(0, rowIndex * rowHeight));
                    addDisplayElement(recipeDisplay);
                    rowIndex++;
                }
            }
        }
    }

    public void dispose() {
        for (UIDisplayElement displayElement : getDisplayElements()) {
            if (displayElement instanceof UIRecipeDisplay) {
                ((UIRecipeDisplay) displayElement).dispose();
            }
        }
    }
}
