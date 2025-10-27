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

/*
 * This class was taken from MCProtocolLib.
 *
 * https://github.com/Steveice10/MCProtocolLib
 */

package com.github.retrooper.packetevents.protocol.world.chunk.palette.generic;

import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.reader.CustomPaletteReader;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

/**
 * A palette backed by a List.
 */
//TODO Equals & hashcode
public class ListPalette extends CustomPaletteReader {

    public ListPalette(int bitsPerEntry) {
        super(bitsPerEntry);
    }

    @Deprecated
    public ListPalette(int bitsPerEntry, NetStreamInput in) {
        super(bitsPerEntry, in);
    }

    public ListPalette(int bitsPerEntry, PacketWrapper<?> wrapper) {
        super(bitsPerEntry, wrapper);
    }

    @Override
    public int stateToId(int state) {
        int id = -1;
        for (int i = 0; i < this.nextId; i++) { // Linear search for state
            if (this.data[i] == state) {
                id = i;
                break;
            }
        }
        if (id == -1 && this.size() < this.data.length) {
            id = this.nextId++;
            this.data[id] = state;
        }

        return id;
    }
}
