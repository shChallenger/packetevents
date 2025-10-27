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
import com.github.retrooper.packetevents.protocol.world.chunk.*;

public class WrapperPlayServerChunkDataColumn extends WrapperPlayServerChunkDataAbstract<WrapperPlayServerChunkDataColumn> {

    public WrapperPlayServerChunkDataColumn(PacketSendEvent event) {
        super(event);
    }

    @Override
    protected boolean doReadBlockLight() {
        return false;
    }

    @Override
    protected boolean doReadSkyLight() {
        return false;
    }

    @Override
    protected void readBiomeData(int expectedReaderIndex, BiomeDataInfo biomeDataInfo) {
        if (biomeDataInfo.hasBiomeData && this.serverVersion.isOlderThan(ServerVersion.V_1_15)) {
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
                biomeDataInfo.biomeDataInts = new int[0];
            } else {
                biomeDataInfo.biomeDataBytes = new byte[0];
            }
        }
    }

    @Override
    protected TileEntity[] readTileEntities() {
        return new TileEntity[0];
    }
}
