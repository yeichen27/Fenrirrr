/*
* Copyright 2019 Nu-book Inc.
* Copyright 2023 Axel Waggershauser
*/
// SPDX-License-Identifier: Apache-2.0

#pragma once

#define ZXING_READERS
#undef ZXING_WRITERS

#define ZXING_ENABLE_1D true
#define ZXING_ENABLE_PDF417 true
#define ZXING_ENABLE_AZTEC true
#define ZXING_ENABLE_QRCODE true
#define ZXING_ENABLE_DATAMATRIX true
#define ZXING_ENABLE_MAXICODE true

// Version numbering
#define ZXING_VERSION_MAJOR 3
#define ZXING_VERSION_MINOR 0
#define ZXING_VERSION_PATCH 2
#define ZXING_VERSION_SUFFIX ""

#define ZXING_VERSION_STR "3.0.2"
