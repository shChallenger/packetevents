/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_16;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import com.github.retrooper.packetevents.protocol.stream.NetStreamInputWrapper;
import com.github.retrooper.packetevents.protocol.stream.NetStreamOutput;
import com.github.retrooper.packetevents.protocol.stream.NetStreamOutputWrapper;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.NibbleArray3d;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.DataPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteFactory;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Handles chunk data from 1.9 to 1.17
 */
public class Chunk_v1_9 implements BaseChunk {

    private static final int AIR = 0;
    private static final int LIGHT_NIBBLES_SIZE = 2048;

    private int blockCount;
    private final DataPalette dataPalette;

    private @Nullable NibbleArray3d blockLight;
    private @Nullable NibbleArray3d skyLight;

    public Chunk_v1_9(int blockCount, DataPalette dataPalette) {
        this(blockCount, dataPalette, null, null);
    }

    public Chunk_v1_9(
            int blockCount,
            DataPalette dataPalette,
            @Nullable NibbleArray3d blockLight,
            @Nullable NibbleArray3d skyLight
    ) {
        this.blockCount = blockCount;
        this.dataPalette = dataPalette;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
    }

    /**
     * @deprecated use {@link #read(PaletteFactory, PacketWrapper, boolean, boolean, boolean, boolean)} instead
     */
    @Deprecated
    public Chunk_v1_9(PaletteFactory factory, NetStreamInput in, boolean hasBlockLight, boolean hasSkyLight) {
        this(factory, in, hasBlockLight, hasSkyLight, false, false, PacketEvents.getAPI().getServerManager().getVersion());
    }

    @Deprecated
    private Chunk_v1_9(PaletteFactory factory, NetStreamInput in, boolean hasBlockLight,
                       boolean hasSkyLight, boolean skipBlockLight, boolean skipSkyLight, ServerVersion version) {
        // 1.14+ includes block count in chunk data
        this.blockCount = version.isNewerThanOrEquals(ServerVersion.V_1_14)
                ? in.readShort() : Integer.MAX_VALUE;
        // singleton palette got added with 1.18 which isn't supported by this chunk section implementation
        this.dataPalette = version.isNewerThanOrEquals(ServerVersion.V_1_16)
                ? DataPalette.read(factory, in, PaletteType.CHUNK, false)
                : DataPalette.readLegacy(factory, in);

        int skip = 0;

        if (hasBlockLight && !skipBlockLight) {
            this.blockLight = new NibbleArray3d(in, LIGHT_NIBBLES_SIZE);
        } else {
            if (hasBlockLight) {
                skip = LIGHT_NIBBLES_SIZE;
            }
            this.blockLight = null;
        }

        if (hasSkyLight && !skipSkyLight) {
            this.skyLight = new NibbleArray3d(in, LIGHT_NIBBLES_SIZE);
        } else {
            if (hasSkyLight) {
                skip += LIGHT_NIBBLES_SIZE;
            }
            this.skyLight = null;
        }

        if (skip != 0) {
            in.skip(skip);
        }
    }

    public static Chunk_v1_9 read(PaletteFactory factory, PacketWrapper<?> wrapper, boolean hasBlockLight,
                                  boolean hasSkyLight, boolean skipBlockLight, boolean skipSkyLight) {
        NetStreamInputWrapper legacyInput = new NetStreamInputWrapper(wrapper);
        return new Chunk_v1_9(factory, legacyInput, hasBlockLight, hasSkyLight, skipBlockLight, skipSkyLight, wrapper.getServerVersion());
    }

    public static void write(PacketWrapper<?> wrapper, Chunk_v1_9 chunk) {
        NetStreamOutputWrapper legacyOutput = new NetStreamOutputWrapper(wrapper);
        write(legacyOutput, chunk, wrapper.getServerVersion());
    }

    /**
     * @deprecated use {@link #write(PacketWrapper, Chunk_v1_9)} instead
     */
    @Deprecated
    public static void write(NetStreamOutput out, Chunk_v1_9 chunk) {
        write(out, chunk, PacketEvents.getAPI().getServerManager().getVersion());
    }

    @Deprecated
    private static void write(NetStreamOutput out, Chunk_v1_9 chunk, ServerVersion version) {
        // 1.14+ includes block count in chunk data
        if (version.isNewerThanOrEquals(ServerVersion.V_1_14)) {
            out.writeShort(chunk.blockCount);
        }

        DataPalette.write(out, chunk.dataPalette);

        if (chunk.blockLight != null) {
            out.writeBytes(chunk.blockLight.getData());
        }
        if (chunk.skyLight != null) {
            out.writeBytes(chunk.skyLight.getData());
        }
    }

    @Override
    public int getBlockId(int x, int y, int z) {
        return this.dataPalette.get(x, y, z);
    }

    public void set(int x, int y, int z, int state) {
        int curr = this.dataPalette.set(x, y, z, state);
        // Pre-1.14 we don't get block counts
        if (blockCount == Integer.MAX_VALUE) return;
        if (state != AIR && curr == AIR) {
            this.blockCount++;
        } else if (state == AIR && curr != AIR) {
            this.blockCount--;
        }
    }

    @Override
    public boolean isEmpty() {
        // Pre-1.14 we have to calculate the value
        if (blockCount == Integer.MAX_VALUE) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (this.dataPalette.get(x, y, z) != AIR) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        // 1.14+, we can rely on the value
        return this.blockCount == 0;
    }

    public @Nullable NibbleArray3d getSkyLight() {
        return this.skyLight;
    }

    public void setSkyLight(@Nullable NibbleArray3d skyLight) {
        this.skyLight = skyLight;
    }

    public @Nullable NibbleArray3d getBlockLight() {
        return this.blockLight;
    }

    public void setBlockLight(@Nullable NibbleArray3d blockLight) {
        this.blockLight = blockLight;
    }
}
