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
package org.terasology.anotherWorld.decorator.layering;

import org.terasology.anotherWorld.util.ChunkRandom;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.LinkedList;
import java.util.List;

public class DefaultLayersDefinition implements LayersDefinition {
    private String seed;
    private List<LayerDefinition> layerDefinitions = new LinkedList<>();

    public void addLayerDefinition(PDist thickness, Block block, boolean generateUnderSee) {
        layerDefinitions.add(new LayerDefinition(thickness, block, generateUnderSee));
    }

    @Override
    public void initializeWithSeed(String seed) {
        this.seed = seed;
    }

    @Override
    public void generateInChunk(int groundLevel, int seeLevel, Chunk chunk, int x, int z) {
        Random random = ChunkRandom.getChunkRandom(seed, chunk.getPos(), 349 * (31 * x + z));
        boolean underSee = groundLevel < seeLevel;

        int level = groundLevel;
        for (LayerDefinition layerDefinition : layerDefinitions) {
            if (!underSee || layerDefinition.generateUnderSee) {
                int layerHeight = layerDefinition.thickness.getIntValue(random);
                for (int i = 0; i < layerHeight; i++) {
                    if (level - i > 0) {
                        chunk.setBlock(x, level - i, z, layerDefinition.block);
                    }
                }
                level -= layerHeight;
                if (level < 1) {
                    break;
                }
            }
        }
    }

    private static class LayerDefinition {
        private PDist thickness;
        private Block block;
        private boolean generateUnderSee;

        private LayerDefinition(PDist thickness, Block block, boolean generateUnderSee) {
            this.thickness = thickness;
            this.block = block;
            this.generateUnderSee = generateUnderSee;
        }
    }
}
