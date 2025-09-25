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

package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.*;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.ChunkReader;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.*;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.BitSet;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public abstract class WrapperPlayServerChunkDataAbstract<T extends WrapperPlayServerChunkDataAbstract<T>> extends PacketWrapper<T> {
    private static final ChunkReader_v1_7 chunkReader_v1_7 = new ChunkReader_v1_7();
    private static final ChunkReader_v1_8 chunkReader_v1_8 = new ChunkReader_v1_8();
    private static final ChunkReader_v1_9 chunkReader_v1_9 = new ChunkReader_v1_9();
    private static final ChunkReader_v1_16 chunkReader_v1_16 = new ChunkReader_v1_16();
    private static final ChunkReader_v1_18 chunkReader_v1_18 = new ChunkReader_v1_18();

    protected Column column;
    protected boolean ignoreOldData;

    public WrapperPlayServerChunkDataAbstract(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerChunkDataAbstract(Column column) {
        this(column, false);
    }

    public WrapperPlayServerChunkDataAbstract(Column column, boolean ignoreOldData) {
        super(PacketType.Play.Server.CHUNK_DATA);
        this.column = column;
        this.ignoreOldData = ignoreOldData;
    }

    @Override
    public void read() {
        int chunkX = readInt();
        int chunkZ = readInt();

        // All chunks are full chunks in 1.17 and above to avoid issues with arbitrary world height
        boolean checkFullChunk = serverVersion.isOlderThan(ServerVersion.V_1_17);
        // Don't read a boolean if there isn't a boolean to be read
        boolean fullChunk = !checkFullChunk || readBoolean();

        if (serverVersion == ServerVersion.V_1_16 || serverVersion == ServerVersion.V_1_16_1) {
            ignoreOldData = readBoolean();
        }

        // There is no bitset on 1.18 and above, instead the SingletonPalette is used to represent a chunk with all air
        BitSet chunkMask = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18) ? null : ChunkBitMask.readChunkMask(this);
        boolean hasHeightMaps = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14);
        NBTCompound heightmapsNbt = null;
        Map<HeightmapType, long[]> modernHeightmaps = null;
        if (hasHeightMaps) {
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
                modernHeightmaps = this.readMap(HeightmapType::read, PacketWrapper::readLongArray);
            } else {
                heightmapsNbt = this.readNBT();
            }
        }

        // 1.7 sends a secondary bit mask for the block metadata
        BitSet secondaryChunkMask = null;
        if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            secondaryChunkMask = ChunkBitMask.readChunkMask(this);
        }

        int chunkSize = 16;
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)) {
            chunkSize = user.getTotalWorldHeight() >> 4;
        }

        BiomeDataInfo biomeDataInfo = new BiomeDataInfo();

        // 1.7 logic is the same
        // 1.8 logic is the same, however, MCProtocolLib checks for remaining bytes... is this needed?
        // 1.9 logic is the same
        // 1.12 logic is the same
        // 1.14 logic for having biome data - is full chunk
        // 1.16 logic is the same
        // 1.17 logic ALWAYS sends biome data because it is always a full chunk
        // 1.18 logic makes it all a palette type system for biome data...
        biomeDataInfo.hasBiomeData = fullChunk && serverVersion.isOlderThan(ServerVersion.V_1_18);

        boolean bytesInsteadOfInts = serverVersion.isOlderThan(ServerVersion.V_1_13);

        // 1.7 sends the chunk data as a byte array of size 256 when it is a full chunk
        // This also applies to 1.8 through 1.12
        //
        // 1.13 uses an integer array of size 256 at the end of chunk data
        // This applies from 1.14
        //
        // 1.16.2+ send a var int array instead of an int array
        if (biomeDataInfo.hasBiomeData && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16_2)) {
            biomeDataInfo.biomeDataInts = readVarIntArray();
        } else if (biomeDataInfo.hasBiomeData && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_15)) {
            biomeDataInfo.biomeDataInts = new int[1024];
            for (int i = 0; i < biomeDataInfo.biomeDataInts.length; i++) {
                biomeDataInfo.biomeDataInts[i] = readInt();
            }
        }

        boolean hasBlockLight = (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16) || serverVersion.isOlderThan(ServerVersion.V_1_14))
                && !serverVersion.isOlderThanOrEquals(ServerVersion.V_1_8_8);
        boolean hasSkyLight = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)
                || this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_8_8)
                || this.user != null && this.user.getDimensionType().hasSkyLight()
                && this.serverVersion.isOlderThan(ServerVersion.V_1_14);

        Object originalBuffer = this.buffer;
        if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            // decompress data and replace contents of this packet wrapper with the chunk data temporarily
            byte[] data = this.inflate(this.readByteArray(), chunkMask, fullChunk);
            this.buffer = UnpooledByteBufAllocationHelper.wrappedBuffer(data);
            biomeDataInfo.dataLength = data.length;
        } else {
            // let the chunk reader decide how to handle reading
            biomeDataInfo.dataLength = this.readVarInt();
        }
        BaseChunk[] chunks;
        try {
            int expectedReaderIndex = ByteBufHelper.readerIndex(this.buffer) + biomeDataInfo.dataLength;
            chunks = this.getChunkReader().read(this.user.getDimensionType(), chunkMask, secondaryChunkMask,
                    fullChunk, hasBlockLight, hasSkyLight, chunkSize, biomeDataInfo.dataLength, this);

            this.readBiomeData(expectedReaderIndex, biomeDataInfo);

            // verify the full chunk has been read
            int readerIndex = ByteBufHelper.readerIndex(this.buffer);
            if (expectedReaderIndex != readerIndex) {
                if (expectedReaderIndex < readerIndex) {
                    throw new RuntimeException("Error while decoding chunk at " + chunkX + " " + chunkZ
                            + "; expected reader index " + expectedReaderIndex + ", got " + readerIndex);
                }
                // we didn't read the whole buffer, skip the rest
                ByteBufHelper.readerIndex(this.buffer, expectedReaderIndex);
            }
        } finally {
            // change buffer back if it has been switched
            if (this.buffer != originalBuffer) {
                ByteBufHelper.release(this.buffer);
                this.buffer = originalBuffer;
            }
        }

        TileEntity[] tileEntities = this.readTileEntities();

        if (biomeDataInfo.hasBiomeData) {
            if (hasHeightMaps) {
                if (bytesInsteadOfInts) {
                    column = new Column(chunkX, chunkZ, true, chunks, tileEntities, heightmapsNbt, biomeDataInfo.biomeDataBytes);
                } else {
                    column = new Column(chunkX, chunkZ, true, chunks, tileEntities, heightmapsNbt, biomeDataInfo.biomeDataInts);
                }
            } else {
                if (bytesInsteadOfInts) {
                    column = new Column(chunkX, chunkZ, true, chunks, tileEntities, biomeDataInfo.biomeDataBytes);
                } else {
                    column = new Column(chunkX, chunkZ, true, chunks, tileEntities, biomeDataInfo.biomeDataInts);
                }
            }
        } else {
            if (hasHeightMaps) {
                if (modernHeightmaps != null) {
                    this.column = new Column(chunkX, chunkZ, fullChunk, chunks, tileEntities, modernHeightmaps);
                } else {
                    this.column = new Column(chunkX, chunkZ, fullChunk, chunks, tileEntities, heightmapsNbt);
                }
            } else {
                column = new Column(chunkX, chunkZ, fullChunk, chunks, tileEntities);
            }
        }
    }

    protected abstract void readBiomeData(int expectedReaderIndex, BiomeDataInfo biomeDataInfo);

    protected abstract TileEntity[] readTileEntities();

    // this step is only needed for 1.7.x
    private byte[] inflate(byte[] input, BitSet mask, boolean fullChunk) {
        // Determine inflated data length.
        int chunkCount = 0;

        for (int count = 0; count < 16; count++) {
            chunkCount += mask.get(count) ? 1 : 0;
        }

        int len = 12288 * chunkCount;
        if (fullChunk) {
            len += 256;
        }

        byte[] data = new byte[len];
        // Inflate chunk data.
        Inflater inflater = new Inflater();
        inflater.setInput(input, 0, input.length);

        try {
            inflater.inflate(data);
        } catch (DataFormatException e) {
            e.printStackTrace();
        } finally {
            inflater.end();
        }

        return data;
    }

    @Override
    public void write() {
        throw new UnsupportedOperationException("Packet is read-only");
    }

    @Override
    public void copy(T wrapper) {
        this.column = wrapper.column;
        this.ignoreOldData = wrapper.ignoreOldData;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public boolean isIgnoreOldData() {
        return ignoreOldData;
    }

    public void setIgnoreOldData(boolean ignoreOldData) {
        this.ignoreOldData = ignoreOldData;
    }

    private ChunkReader getChunkReader() {
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            return chunkReader_v1_18;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
            return chunkReader_v1_16;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            return chunkReader_v1_9;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8)) {
            return chunkReader_v1_8;
        } else {
            return chunkReader_v1_7;
        }
    }

    protected static class BiomeDataInfo {
        public int dataLength;
        public boolean hasBiomeData;
        public int[] biomeDataInts = null;
        public byte[] biomeDataBytes = null;
    }
}
