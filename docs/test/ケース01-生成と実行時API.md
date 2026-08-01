# ケース01: 生成と実行時API

本資料はテスト戦略 §2 の K1(正値)・K3・K5・K6・K9・K10・K11・K15 × 観測面 O1/O4 の正典である。  
K9 の表記変種（import 別名・FQN・star import・typealias）と診断の発火/非発火はケース04、可視性（K8・規則 2/3・IR-only アクセサ）はケース02 が正典である。  
順序スナップショットと sealedSubclasses 対照はケース03、跨 module / MPP / Java 観測はケース05、IC・決定性はケース06 が正典である。  
実装列はフィクスチャ構成.md のテストクラス名#想定メソッド名（計画名・英語 lowerCamel）。

## 1. 基本契約（si・K1=sealed interface）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-01 | K1=SI × kind 解決 2 分岐 | 値→kind の全経路（asEnumish / entries / valueOf）が同一シングルトンへ収束・class 系=companion / object=自身・kind 同一性は `===`・異インスタンスも単一 kind | SiContractTest#entriesHoldKindSingletons | V1 |
| API-02 | K15=遅延初期化 | entries は毎回同一 List 参照（lazy 一度・memoize） | SiContractTest#entriesAreMemoized | V2-b |
| API-03 | O1=valueOf 契約 | 完全一致のみ解決（部分一致・大小無視・空/空白は不一致）・失敗は IllegalArgumentException で文言 `No enumish entry with label 'X' in SI`（simpleName）・valueOfOrNull は null | SiContractTest#valueOfContractAndFailureMessage | 文言の全プラットフォーム一致→ケース05 |
| API-04 | O1=往復整合 | 全 entries で valueOf(label) 往復一致・enumishCompanion 経由でも同一 | SiContractTest#valueOfRoundTripsAllEntries | — |
| API-05 | O1=label | label 拡張=kind label・asEnumish().label が確実な取得経路 | SiContractTest#labelExtensionReadsKindLabel | — |
| API-06 | O1/O4=enumizedClass | enumizedClass=末端 KClass・共変連鎖が `List<KClass<out SI>>` に型付く | SiContractTest#enumizedClassChainIsCovariant | — |
| API-07 | O1/O4=enumishCompanion | 全 kind で階層 Companion の同一シングルトン返却・共変 override で entries が `List<SI.Enumish>` に型付く | SiContractTest#enumishCompanionIsSharedAndCovariant | — |
| API-08 | O4=asEnumish 規則 1 | 返り値型=具体型 companion 型（規則 1） | SiContractTest#asEnumishReturnsConcreteCompanionType | 規則 2/3→ケース02 |
| API-09 | O4=when 網羅性 | kind-when は同一 module で else 省略成立・枝形 3 種（companion 等値/短縮形/is）動作・kind-when は値型を絞らず値 when はスマートキャスト | SiContractTest#whenBranchShapesAndExhaustiveness | V1 |
| API-10 | O4=値/kind 型分離 | companion kind は SI.Enumish であり SI の値でない・object 末端は両方 | SiContractTest#kindIsNotAHierarchyValue | — |
| API-11 | O4=共変束ね | 複数階層の Companion を `List<EnumishCompanion<Enumish>>` へ射影なしで束ね各 entries 取得（out 共変） | SiContractTest#companionsBundleWithoutProjection | si+enumleaf の 2 階層使用 |

## 2. 末端種別と吸収（zoo・K3）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-12 | K3=全種別 × K6=自動生成 | 10 種別末端（data class/data object/object/final class/open/abstract/interface/fun interface/enum/value class）で 1 末端=1 kind・label=単純名・object 系=自身/他=companion（自動生成 companion 含む） | ZooTest#oneKindPerLeafAcrossAllShapes | V3・V10 |
| API-13 | K15=多段サブタイプ | 吸収: open 直下・多段・object サブタイプ・interface 第三者実装（default asEnumish の JVM lowering 込み）・テスト内無名 object 実装・テスト内 local class 実装・末端 interface への委譲実装（`: IfaceLeaf by impl`）は kind 新設せず entries 不変・asEnumish 継承 | ZooTest#subtypesAreAbsorbedIntoLeafKinds | V10・AK 非発火→ケース04・基底直接委譲は DIA-69 |
| API-14 | K3=非 final 末端 | 非 final 末端の enumizedClass=分類代表（実行時クラスと不一致可） | ZooTest#enumizedClassIsRepresentativeForOpenLeaves | V10 |
| API-15 | K3=interface 末端下限 | 実装者ゼロの interface 末端（Ghost・自動 companion）も entries 掲載 | ZooTest#implementorlessInterfaceLeafHasKind | V10 |
| API-16 | O4=値 when | 値 when の is 末端枝がサブタイプを被覆（sealed 地力） | ZooTest#valueWhenCoversSubtypesByLeafBranch | V10 |
| API-17 | K15=SAM 変換 × K6=明示/自動 | fun interface 末端の SAM 変換値も同一 kind・生成後も SAM 保持（asEnumish は default 実装）・明示 public companion（FunLeaf/IfaceLeaf=kind は明示 companion・default asEnumish がそれを返却）と自動生成（FunAuto/Ghost）の両分岐 | ZooTest#samConversionSharesKind / ZooTest#explicitCompanionInterfaceLeavesResolveKind | V10 |
| API-18 | K15=boxing | value class 末端の inline/boxed 双方で kind 同一・label 拡張・asEnumish 安定 | ZooTest#valueClassKeepsKindAcrossBoxing | — |

## 3. enum 末端（enumleaf・K3=enum class）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-19 | K3=enum × kind 粒度 | enum 末端は全体で 1 kind・定数非展開・複数 enum は各 1 kind（自動 companion 含む） | EnumLeafTest#eachEnumLeafIsExactlyOneKind | V4・V3 |
| API-20 | O1=name/label 分離 | `Enum.name` と label の併存・enum 定数名は label 領域外（valueOf("HELP")=同名 data object / valueOf("Builtin")=enum kind） | EnumLeafTest#nameAndLabelDomainsAreSeparate | V4 |
| API-21 | K10=toString × enum | enum kind の toString（2 原則で生成）と定数側 toString override は管轄の異なる別物・相互無影響 | EnumLeafTest#kindAndConstantToStringAreIndependent | V4・V11 |
| API-22 | K6=明示 companion × enum | 明示 companion の kind 流用・`Verb.entries` / `SI.Enumish.entries` / kind の 3 名前空間併存・asEnumish/enumizedClass/valueOf 成立 | EnumLeafTest#explicitCompanionIsReusedAsKind | V4 |

## 4. companion 状態（plain・K6）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-23 | K6=明示既定名・共存 | 明示既定名 companion の kind 流用（重複生成回避）・ユーザーメンバーと生成メンバーの共存 | PlainCompanionTest#explicitCompanionIsReusedWithUserMembers | — |
| API-24 | K6=名前付き companion | 名前付き companion Factory=kind・label は末端単純名で不変（companion 名非依存）・valueOf は companion 名を解決しない | PlainCompanionTest#namedCompanionIsKindButLabelIsLeafName | 可視性 4 値→ケース02 |

## 5. companion 末端（companionleaf・K3=階層外クラスの companion）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-25 | K3=既定名 companion 末端 | 階層外クラスの既定名 companion 単独末端は許容・kind=companion 自身・label="Companion" | CompanionLeafTest#defaultNameCompanionLeafIsAllowed | 外側=末端は CLC→ケース04 |
| API-26 | K3=名前付き companion 末端 | 名前付き companion 末端の label=宣言名 | CompanionLeafTest#namedCompanionLeafUsesDeclaredName | 序数境界→ケース03 |
| API-52 | K6=基底/中間の companion 末端 | 基底自身の companion（`companion object : Task()`）・中間 sealed の companion（`: RootVia`）はいずれも末端として成立（CLC は外側=末端のみ検査）・kind=自身・label=宣言名・entries 掲載 | SealedClassBaseTest#baseCompanionLeafIsAllowed / MidTrackingTest#intermediateCompanionLeafIsAllowed | K6 三分法の成立側 2 極 |

## 6. sealed class 基底（sealedbase・K1=sealed class × K9）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-27 | K1=sealed class × K9=コンストラクタ呼び出し形 | `:Task()` 形 supertype でも所属判定・companion 自動生成が成立 | SealedClassBaseTest#constructorCallSupertypeWorks | V3 |
| API-28 | K1=sealed class × K3=object | sealed class 基底の object / data object 末端=自身が kind・toString 生成差分（非 data=生成 / data=言語合成） | SealedClassBaseTest#objectLeavesOfClassBase | V11 |

## 7. 中間 sealed（mid・K5 × K9）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-29 | K5=1 段 × K9=中間経由再帰追跡 | 中間 sealed class / sealed interface 経由の raw 追跡再帰で末端 companion 自動生成 | MidTrackingTest#leavesViaIntermediatesGetAutoCompanions | V3 |
| API-30 | K5=中間非生成 | 中間には何も生成されず entries 非掲載（末端まで平坦化）・中間の明示 companion へも Enumish 非注入（kind 非成立）・中間型変数の asEnumish は末端 kind へ実体解決 | MidTrackingTest#intermediatesHaveNoKind | 多段の展開順→ケース03 |
| API-51 | K9=スコープ順の競合解決 | raw 追跡の優先関係を競合 3 形で固定: star import 基底 vs 同一 pkg 同名非基底=同一 pkg 勝ち（entries 非所属）・同一 pkg 同名 vs 明示 import 基底=import 勝ち（所属）・明示 import vs 外側ネスト同名=ネスト勝ち（非所属） | RawTrackingTest#scopePriorityDecidesMembership | V3。表記単独形の成立→ケース04 DIA-31/32 |
| API-57 | K5=多重経路（兄弟中間の同時実装） | 複数経路で到達する末端は初出位置に 1 回だけ entries 掲載・kind は 1 つ（どちらの中間型からも同じ kind へ解決）・生成 Enumish の継承者一覧も重複せず kind-when は else 不要 | MultiPathTest#multiPathLeafIsListedOnceAtFirstOccurrence / MultiPathTest#multiPathLeafHasSingleKind | 基底は一意のため診断は非発火。展開順の規則→ケース03 §1 |

## 8. 型パラメータ（generic・K11）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-31 | K11=基底のみ/末端のみ/両方/変位 | 生成 Enumish・companion kind は無型パラ・型引数は kind 同一性に無関係（基底のみ/末端のみ/両方・out 変位注釈末端の各構成で成立） | GenericTest#typeArgumentsDoNotAffectKind | — |
| API-32 | K11=star projection (O4) | enumizedClass=star projection の末端 KClass・基底型変数から label 拡張成立 | GenericTest#enumizedClassIsStarProjected | — |

## 9. toString 2 原則（manual.tostr・K10）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-33 | K10=原則 2（生成形） | 明示実装の無い kind（companion kind・非 data object）へ toString=label を必ず生成 | ToStringTest#kindsWithoutExplicitToStringGetLabel | V11・si/plain でも観測 |
| API-34 | K10=原則 1(a) 手動宣言 | kind 自身の手動 toString を尊重し非生成（data object 手動 Manual・companion 手動 Styled） | ToStringTest#manualToStringIsRespected | MC 対象外の非発火→ケース04 |
| API-35 | K10=原則 1(b) 継承具象 | 継承経路の Any 以外の具象 toString で非生成（companion 継承 ViaBase・object 自身継承 ObjViaBase）・final 継承 ViaFixed も衝突なしスキップ | ToStringTest#inheritedConcreteToStringSkipsGeneration | V11 |
| API-36 | K10=原則 1(c) data object | data object は言語合成 toString のまま非生成・data class 値インスタンスの data 合成 toString も kind 側と管轄分離で不変 | SiContractTest#dataObjectKeepsSynthesizedToString | V11 |
| API-37 | K10=抽象再宣言 | supertype の toString 抽象再宣言は kind 側手動実装で充足（生成は充足に使えない） | ToStringTest#abstractRedeclarationSatisfiedManually | 未実装時の言語エラー→ケース04 |
| API-38 | K10=label 独立性 | label は toString 分岐と無関係に常に生成 | ToStringTest#labelIsAlwaysGenerated | — |
| API-39 | K10=label メンバー | メンバー label が拡張 label をシャドー・valueOf は kind label 基準・確実経路は asEnumish().label | ToStringTest#memberLabelShadowsExtension | ES 警告の発火→ケース04 |

## 10. 手動実装（manual.impl・K10）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-40 | K10=階層内手動実装 | 階層内の生成 Enumish 手動実装（open・internal 両形）は許容・値は entries/valueOf 非掲載・Enumish として機能 | ManualImplTest#manualImplIsAllowedButUnlisted | V1・階層外は MIOH→ケース04 |
| API-41 | O4=手動実装 × 網羅性 | 手動実装は inheritors 掲載で kind-when に is 枝が必要・open 手動実装のサブタイプは inheritors 非追加（既存 is 枝が被覆＝枝は増えない） | ManualImplTest#kindWhenRequiresManualBranch / ManualImplTest#manualImplSubtypeDoesNotJoinInheritors | V1・跨 module 面→ケース05 |
| API-42 | K10=基底 Enumish 実装 | 基底 Enumish の自由実装（階層外）は無制約・どの階層の entries にも非掲載 | ManualImplTest#freeBaseEnumishImplIsUnrestricted | — |

## 11. label 閉域（manual.samelabel）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-43 | O1=階層間閉域 | 独立 2 階層の同名末端: 各 valueOf は自階層のみ照合・entries 独立・他階層 label へ非漏出 | SameLabelTest#hierarchiesDoNotLeak | 同一階層内衝突は LC→ケース04 |

## 12. 実行時状態（reentry・K15）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-44 | K15=遅延初期化 | entries は初回アクセスまで末端未初期化・以降再構築なし（InitProbe 観測） | ReentryTest#entriesAreBuiltLazily | V2-b |
| API-45 | K15=初期化子再入 | kind 初期化子からの entries/valueOf/valueOfOrNull 参照は JVM 実測で SOE せず二重実行完了（再入禁止事項の実挙動固定） | ReentryTest#initializerReentryCompletesTwice | 3 経路の独立階層 |
| API-46 | K15=非再入 API | asEnumish/label/enumishCompanion は lazy 非接触で初期化中も安全 | ReentryTest#nonLazyApisAreSafeDuringInit | — |
| API-47 | K15=マルチスレッド初回 | 並行初回アクセスでも lazy(SYNCHRONIZED) により単一 List 構築 | LazyRaceTest#concurrentFirstAccessYieldsSingleList | フィクスチャは mpp-producer jvmMain(Raced)・テストは jvmTest |

## 13. 空/単一境界（bounds）

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-48 | K5=継承者ゼロ | 空階層: entries 空かつ memoize・valueOf 常時例外＋文言・valueOfOrNull=null・診断なし | BoundsTest#emptyHierarchyContract | — |
| API-49 | K5=下限境界 | 単一末端階層・継承者ゼロ中間（空展開）でも全 API 成立 | BoundsTest#singleLeafHierarchyWorks | 順序面→ケース03 |
| API-50 | O4=順序 API 非公開 | 生成 Enumish・kind は ordinal 相当メンバー・Comparable を提供しない（enum との意図的差異・順序取得は entries 限定）を NG コメントと実行時型検査で固定 | SiContractTest#kindsExposeNoOrdinalOrComparable | 序数永続化禁止の根拠 |

## 14. label カスタマイズ（K16）

変換規則（単語分割・ロケール非依存）の単体固定は compiler-plugin の EnumizeLabelCaseTest が担い
（kotlinx.serialization 準拠の期待値列挙）、本節は生成物経由の実行時観測を担う。  
プロジェクト既定の DSL 指定側はケース06 BLD-48、衝突・不正付与の発火側はケース04 DIA-72〜75。

| ID | 次元/値 | 観測と期待 | 実装 | 備考(関連V/診断) |
|---|---|---|---|---|
| API-53 | K16=labelCase 一律適用 | @Enumize(labelCase=UPPER_SNAKE_CASE) が enum 末端の kind を含む全末端へ適用・valueOf は最終 label 照合（変換前の単純名は不一致） | LabelCustomizationTest#labelCaseAppliesToAllLeaves | 変換規則自体は EnumizeLabelCaseTest |
| API-54 | K16=明示 label 優先 | @EnumishLabel は変換より優先・kind の toString は label へ追随・data object の toString は言語合成のまま乖離 | LabelCustomizationTest#explicitLabelWinsAndToStringFollows | 概要 §4 原則 1 |
| API-55 | K16=PROJECT_DEFAULT 明示 | labelCase=PROJECT_DEFAULT の明示指定はプロジェクト既定（DSL 未設定 = convention の AS_DECLARED）へ解決 | LabelCustomizationTest#projectDefaultResolvesToProjectSetting | DSL 指定側は BLD-48 |
| API-56 | K16=明示 label による衝突解消 | 同一単純名の 2 末端を @EnumishLabel で解消・entries の順序（ClassId 由来）は label 値に不干渉 | LabelCustomizationTest#aliasResolvesSimpleNameClash | 発火側は DIA-74/75 |
