package com.shawen.compress;

import com.shawen.common.extension.SPI;

@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
