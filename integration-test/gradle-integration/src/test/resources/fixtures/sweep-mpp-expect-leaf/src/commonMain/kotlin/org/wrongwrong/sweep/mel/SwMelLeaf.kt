package org.wrongwrong.sweep.mel

// TC-MPP-049: 末端が expect class（基底は common の非 expect）。actual は platform ソースセットに
// 存在するため、sealed の同一ソースセット制約 or expect/actual マッチングの言語診断へ合流する観測点
expect class SwMelLeaf() : SwMel
