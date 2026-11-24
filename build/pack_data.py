"""Modrinth file table for THE LAST LIGHT (Fabric 1.21.11).

Every entry was resolved against the Modrinth API in November 2026:
version id, CDN url, sha1/sha512 and byte size are the ones Modrinth
reports for that exact file. Do not "fix" versions by hand - re-query
https://api.modrinth.com/v2/project/<slug>/version instead.
"""

# path, url, sha1, sha512, size, env
BOTH = {"client": "required", "server": "required"}
CLIENT = {"client": "required", "server": "unsupported"}

FILES = [
    # ------------------------------------------------------------------ base
    ("mods/fabric-api-0.141.6+1.21.11.jar",
     "https://cdn.modrinth.com/data/P7dR8mSH/versions/6qAuTtLR/fabric-api-0.141.6%2B1.21.11.jar",
     "c98467cbbaf4d197377266795ae015f4130d65b6",
     "852d3682c4f353fd0ca86546af8578513e8bcf628377fa9d49c556bc59c5024c69ab56c92fc6dfba11fed19e4316a3f5636e7e07fd3d96f7043b531aef59b637",
     2426039, BOTH),

    ("mods/cloth-config-21.11.153-fabric.jar",
     "https://cdn.modrinth.com/data/9s6osm5g/versions/xuX40TN5/cloth-config-21.11.153-fabric.jar",
     "4c224606a963bce223db5b27edb4959ecf40d4ee",
     "8f455489d4b71069e998568cf4e1450116f4360a4eb481cd89117f629c6883164886cf63ca08ac4fc929dd13d1112152755a6216d4a1498ee6406ef102093e51",
     1148427, BOTH),

    ("mods/placeholder-api-2.8.2+1.21.10.jar",
     "https://cdn.modrinth.com/data/eXts2L7r/versions/qxjzQ9xY/placeholder-api-2.8.2%2B1.21.10.jar",
     "bbdc920ecff993148cd6e81a208536132245ce40",
     "507ab10b7938dcd14d33121b8462649bdbe575cef248e917dfdf7566078ab5d0195ca1add95eae4863de3f652eb56db0a8669a67d5b5344e094d086f9dab5a08",
     268662, BOTH),

    # NOTE: GeckoLib + VillagersTradingPlus were removed in 1.0.2 together
    # with The Graveyard (user request - no story/lore content). Nothing
    # else in the pack needs them.

    ("mods/ForgeConfigAPIPort-v21.11.1-mc1.21.11-Fabric.jar",
     "https://cdn.modrinth.com/data/ohNO6lps/versions/uXrWPsCu/ForgeConfigAPIPort-v21.11.1-mc1.21.11-Fabric.jar",
     "61aa1b5fafd75afc44552d0213cac39bc938bc2d",
     "28791c992d613da14b8685505d3ef632ed53b5f1e1d517f0b41677d10f8419f192dfbde991308df6cda5d0f113c0aa8fc18ecf4a0834029403b16d2f68dc52d6",
     598009, BOTH),

    # ------------------------------------------------------- performance core
    ("mods/sodium-fabric-0.8.14+mc1.21.11.jar",
     "https://cdn.modrinth.com/data/AANobbMI/versions/rkdTcxoT/sodium-fabric-0.8.14%2Bmc1.21.11.jar",
     "4a8733fe544168068d12fa94b2ab03e6c0c7bf91",
     "04c43f9e8534b87a52c42ffd51b0e344d4ef92dc9cc52da33d13af6a18bf74b05380d2027f1e0db982698d0aff274c42741c1e21b313b0cd75ed3af005fc98d9",
     1909150, CLIENT),

    ("mods/lithium-fabric-0.21.4+mc1.21.11.jar",
     "https://cdn.modrinth.com/data/gvQqBUqZ/versions/Ow7wA0kG/lithium-fabric-0.21.4%2Bmc1.21.11.jar",
     "203bdcb26e97b3217b045e1182651a7d7b6462ec",
     "f14a5c3d2fad786347ca25083f902139694f618b7c103947f2fd067a7c5ee88a63e1ef8926f7d693ea79ed7d00f57317bae77ef9c2d630bf5ed01ac97a752b94",
     900462, BOTH),

    ("mods/ferritecore-8.2.0-fabric.jar",
     "https://cdn.modrinth.com/data/uXXizFIs/versions/Ii0gP3D8/ferritecore-8.2.0-fabric.jar",
     "c2e2f5e008f5bc9f401dfad8ca94909917006b65",
     "3210926a82eb32efd9bcebabe2f6c053daf5c4337eebc6d5bacba96d283510afbde646e7e195751de795ec70a2ea44fef77cb54bf22c8e57bb832d6217418869",
     80138, BOTH),

    ("mods/ImmediatelyFast-Fabric-1.14.3+1.21.11.jar",
     "https://cdn.modrinth.com/data/5ZwdcRci/versions/4EwhsTu7/ImmediatelyFast-Fabric-1.14.3%2B1.21.11.jar",
     "43e451464f979bdf9ff4ac288b358a2f91b79ed8",
     "2564cb28259352c3aa5759aceca62667de08a7860a40910dad40a043af851a074b2d75cef50b9fa6f1971febb3a5ecebe4067cbd4b60e63ef711b85f4bc21407",
     302709, CLIENT),

    ("mods/entityculling-fabric-1.12.2-mc1.21.11.jar",
     "https://cdn.modrinth.com/data/NNAgCjsB/versions/bTu42fvx/entityculling-fabric-1.12.2-mc1.21.11.jar",
     "6e3ea8f9c8bc14ac91328314f4b270b6cf6927ab",
     "049c01bf688fe2ddf1baba2223943b3437dea8e706982417bc4ede2547c664222a62b2842706c6789bb9853cf015544395beedd588fcd9608bf5091d8f927e9c",
     1543912, CLIENT),

    ("mods/moreculling-fabric-1.21.11-1.6.2.jar",
     "https://cdn.modrinth.com/data/51shyZVL/versions/wOzykoLV/moreculling-fabric-1.21.11-1.6.2.jar",
     "11a929de62650cc536244d4c2ba889fa84c1c0f2",
     "58cec88e84835c543d8604411f89a0984abe51f0332860354355ede290d2442fc33a86ecda1766e0b1a82ba4edfab27f3c9f6855c0af3234e200218b892756f2",
     340975, CLIENT),

    ("mods/sodium-extra-fabric-0.9.5+mc1.21.11.jar",
     "https://cdn.modrinth.com/data/PtjYWJkn/versions/efIKaD8Q/sodium-extra-fabric-0.9.5%2Bmc1.21.11.jar",
     "131a81641152efd5feb90bdb643d82726ec1da89",
     "60716a8f0780dcedc230dda3c93265d55d823313a0846d6e7da482ed0c38506e04079ab72629c8c2201f1b248431d0037fc44e9be89e664b507be53a9a1668f4",
     487570, CLIENT),

    ("mods/iris-fabric-1.10.8+mc1.21.11.jar",
     "https://cdn.modrinth.com/data/YL57xq9U/versions/ka9PDlsN/iris-fabric-1.10.8%2Bmc1.21.11.jar",
     "acfbe6d065b6c35d85c14da04d35e073363f3acf",
     "f0cad4b6af593b6fc25738c4ef5a2b346125f9693d25a0d486858e54798d2ea87b24d1497e1bb4391729d7d9e490f42771f7fb468d4e1420e372d8643f9e2055",
     2807718, CLIENT),

    ("mods/dynamic-fps-3.11.6+minecraft-1.21.11-fabric.jar",
     "https://cdn.modrinth.com/data/LQ3K71Q1/versions/Fab7e5Th/dynamic-fps-3.11.6%2Bminecraft-1.21.11-fabric.jar",
     "2e5ab45e69b43812491cfa34f8f7a4ce7623c233",
     "c45652252e2176cb8e2c44dfd9032c617135a100dcf161b9e6e6717803c14d1359d0f940fa10dc1aa6ad80669983f4b9c5636c0c2953d3314e06f893f0cdc736",
     219961, CLIENT),

    ("mods/modmenu-17.0.1.jar",
     "https://cdn.modrinth.com/data/mOgUt4GM/versions/cLkYaVuL/modmenu-17.0.1.jar",
     "d5612d70bda58e0179b177ca9990e57471e67d19",
     "cb68cc39c8da66d05a1d2054a0e7b62144881e0cdb5fd681bc4f337b1313fafac394246f4e0aa0c4b6343db9e9750250a00c1bb72689b762da685ce1ccfee51f",
     960882, CLIENT),

    ("mods/reeses-sodium-options-fabric-2.2.4+mc1.21.11.jar",
     "https://cdn.modrinth.com/data/Bh37bMuy/versions/ffpCc8nY/reeses-sodium-options-fabric-2.2.4%2Bmc1.21.11.jar",
     "295623b7f0bd9ffb365e2364d7229b6ed1e2bedc",
     "224e324effcaa03a38582b05c09cbd79a5922e916888b45849d966a6a7f30a7be18a8cc2ea09f44e79e6ea1a8d143a28c3cbfafdb398025b48ab1a1a7d71fd49",
     258250, CLIENT),

    ("mods/krypton-0.2.10.jar",
     "https://cdn.modrinth.com/data/fQEb0iXm/versions/O9LmWYR7/krypton-0.2.10.jar",
     "94ddaeeae6ca7fecc79e54a0aa02a5438a775a99",
     "4dcd7228d1890ddfc78c99ff284b45f9cf40aae77ef6359308e26d06fa0d938365255696af4cc12d524c46c4886cdcd19268c165a2bf0a2835202fe857da5cab",
     270195, BOTH),

    ("mods/fastquit-3.1.3+mc1.21.11.jar",
     "https://cdn.modrinth.com/data/x1hIzbuY/versions/ip2tVKLp/fastquit-3.1.3%2Bmc1.21.11.jar",
     "9ba127357e1fc9ff800496250fb510f70f394714",
     "29f080e82b58833cc2bb6af677c7b49825dbfeee8340d6952aadacbf83a3f5cdb71e2d1145b0e60f7454274577930c3600f3fe8a6f95cb5a423fa6f80d409a01",
     61123, CLIENT),

    # ------------------------------------------------------------- atmosphere
    ("mods/atmosfera-2.7.4+mc1.21.11.jar",
     "https://cdn.modrinth.com/data/28G1xpgF/versions/DRyUTqJO/atmosfera-2.7.4%2Bmc1.21.11.jar",
     "c1a8ec30605f3d2c9cdae31a7655b590c15fcd9d",
     "4aabdcbf700c5d68e9d78ab95bb494581b1959d4e5c06279496ca2e58ea74735e0305e7309989b02892947fa0bc4c5aea96cd6c6bd73e8ae8f24e0584afb73d6",
     64187376, CLIENT),

    ("mods/PresenceFootsteps-1.12.4+1.21.11.jar",
     "https://cdn.modrinth.com/data/rcTfTZr3/versions/xjmToylJ/PresenceFootsteps-1.12.4%2B1.21.11.jar",
     "65d1454890b23f339da0dd95519f824aaa11bb35",
     "9d71f487b8b0bb08d752cd1e40ca1ef74d9701bcd395c02c5d25761f718acbe3493c1d84cca4a371fd8733e72799936206730b81cfc8827b05d4173341fa4d7d",
     5757598, CLIENT),

    ("mods/sound-physics-remastered-fabric-1.21.11-1.5.2.jar",
     "https://cdn.modrinth.com/data/qyVF9oeo/versions/61lf1wKt/sound-physics-remastered-fabric-1.21.11-1.5.2.jar",
     "eaa2ab3dc03f304f62a7beb7090a69594ea57df2",
     "abb5dece10b84ab9f0a658a18ae4f9d2e9928322b3133ce320570f6f80cd4621ba94a17220001372d9d810205d0bf6bdbd61606e5b1d443cb2a00a1df0ad178a",
     204180, CLIENT),

    ("mods/cave_dust_rethinking-3.4.0+1.21.11-fabric.jar",
     "https://cdn.modrinth.com/data/7pdlDpFR/versions/kVAE5J7n/cave_dust_rethinking-3.4.0%2B1.21.11-fabric.jar",
     "72f47df5b7cf705dc7bd5e8d68025d3df875144f",
     "d963ebcad7a624823255429cbfe915ebb4d62be731e4ff6984c401e266b992acb1ec53a34f8cfcd2e565f8c65cfc37a4f71dda39ff6091aaa3288d3702db0cfd",
     134168, CLIENT),

    ("mods/fallingleaves-2.0.3+1.21.11.jar",
     "https://cdn.modrinth.com/data/WhbRG4iK/versions/U7hPBr6x/fallingleaves-2.0.3%2B1.21.11.jar",
     "7cc068a9930b5bd446379a4ff33068808c5e5aa3",
     "6b1ab0536d19ed421d03a678f58c86489e78f349639319cba20035b1950726caf42c53e27b6062aa984b73e9ffebe14f0241d82c070b537de46dd8c867521816",
     181268, CLIENT),

    ("mods/Drip Sounds-0.6.3+1.21.11-Fabric.jar",
     "https://cdn.modrinth.com/data/T8MMXTpr/versions/Vtnu3d7x/Drip%20Sounds-0.6.3%2B1.21.11-Fabric.jar",
     "2c7e2cdfd0917ec2d6c57ad25f65a58a1550b7a8",
     "7a899251b1fcc136fd242a8cb696baafc15ada8f32882107161d981a24d6d059036a264e4f1bdc43c6ba0a7de19050b2050b80afc5cb95965e3b1ea02f3e2e0d",
     70360, CLIENT),

    ("mods/wakes-0.5.1+1.21.11.jar",
     "https://cdn.modrinth.com/data/dlNu0RQY/versions/4zYKqDML/wakes-0.5.1%2B1.21.11.jar",
     "4d2cf78a2202aa2a8bd06eb25b79e0a02d31c0db",
     "7492f5aa196643d2777ab6dded437d5201674ee4e44b0e36d9890ff1fb73243718e939f0bf0fc57fce2a71092725f93632805a963d03cce5e7baa828e7efcdf1",
     282024, CLIENT),

    ("mods/particlerain-4.0.4+1.21.11-fabric.jar",
     "https://cdn.modrinth.com/data/nrikgvxm/versions/vH32Nh26/particlerain-4.0.4%2B1.21.11-fabric.jar",
     "8fe97db93840021c411883dfd2cf6d2f0f774561",
     "f619f64f9f35179f08816070c6b80ce30dc8bd8d7c68dd3aa2bfe872bef4743c4f6d510d59c1df2c70f3289cf396658d76307f23a030889cac1bcb55641911e8",
     629525, CLIENT),

    # ------------------------------------------------------------------ world
    # NOTE: Abandoned Structures 1.0.0 was removed in 1.0.1 - its mixin
    # targets ServerPlayer.drop() which no longer exists in 1.21.11
    # (crash: InvalidInjectionException method_37413). No fixed release
    # exists on Modrinth. Structory covers the same niche.
    ("mods/Structory_26.2_v1.3.7.jar",
     "https://cdn.modrinth.com/data/aKCwCJlY/versions/TUbwu7eG/Structory_26.2_v1.3.7.jar",
     "8a9a1a1b3f74398310420b6fbe2c26ef142bed42",
     "4178a15f32f6ed3d59a7eb2222a8e3f539efd7b54d8ffb790cbf1dfe5c54c6ce6ec33220e148424109c6360c4c6f081b9fbebfe74745fe3c8f05619a06ccb9ec",
     1261346, BOTH),

    # NOTE: The Graveyard was removed in 1.0.2 (user request - the 36-page
    # lore chronicle / story content was unwanted).

    ("mods/lostcities-1.2.0+1.21.9-1.21.11.jar",
     "https://cdn.modrinth.com/data/tsxv5KHT/versions/i7mir4E4/lostcities-1.2.0%2B1.21.9-1.21.11.jar",
     "deaac86dd5f9a140e872449fdfddd022b9c7ff6b",
     "15342a9b9907a395f4e23d65fe42bf4ef1db39fa6486ea6581c3c6da22fcc0beb834ddcc2945f8bb0de72ff89722ab186c5358d1030b3067c27bda3800573db2",
     1404245, BOTH),

    # ----------------------------------------------------------------- horror
    ("mods/adams-echo-0.1.0.jar",
     "https://cdn.modrinth.com/data/itPYV6wk/versions/GELtAdS2/adams-echo-0.1.0.jar",
     "b3cc64bf7b15aca3115c6b276bdfac80b99a5669",
     "9545c20e699ef9a1510f9b39eacf2f58c9976c1e89d2060bebc62bbd6c394a02956a7402f2c4a31b93f12aa8eb2e5d495f6b3c170dd00eb7fc251a2e75d05690",
     15446420, CLIENT),

    # ------------------------------------------------- curator additions
    ("mods/appleskin-fabric-mc1.21.11-3.0.8.jar",
     "https://cdn.modrinth.com/data/EsAfCjCV/versions/59ti1rvg/appleskin-fabric-mc1.21.11-3.0.8.jar",
     "441fdbf4e9c34fb61517ceb8e15618950dc4d314",
     "d32206cb8d6fac7f0b579f7269203135777283e1639ccb68f8605e9f5469b5b54305fd36ba82c64b48b89ae4f1a38501bfb5827284520c3ec622d95edcfa34de",
     180107, BOTH),

    ("mods/notenoughanimations-fabric-1.12.7-mc1.21.11.jar",
     "https://cdn.modrinth.com/data/MPCX6s5C/versions/qr1Bu9qK/notenoughanimations-fabric-1.12.7-mc1.21.11.jar",
     "6af0c07e9963a45a5d4673b1f7f519437fa21fb7",
     "f3b82fe757c16a969544f30daedc9de4100e93003c7fab328bb506274e86846823e89802b4cecdfea42ec31090827d490f1c40235010f62b6c91f6ec834f7aff",
     1886773, CLIENT),

    # --------------------------------------------------------------- shaders
    ("shaderpacks/Kefir's-flashlight.zip",
     "https://cdn.modrinth.com/data/wwfPGOds/versions/LCNLWfDo/Kefir%27s-flashlight.zip",
     "ce6fe56f650b9fb30917e441b23b9deac76c2721",
     "00b1808b1f3bb0555bd56ae2b140d7c2fd4a232b1dfb4b6dd9cb82d0469ae42d9bdfb9368f8c9136e7b841635a1ec3d04f4f7a13784f79ac05d146c671589cdc",
     10469, CLIENT),

    ("shaderpacks/HyShaders Horror.zip",
     "https://cdn.modrinth.com/data/NV4Fv1Zc/versions/lyxqRsrZ/HyShaders%20Horror.zip",
     "06bda4f92d809849de1dcb600edbe795090bea8b",
     "7a94a69d57c43286e813d2a374b60790b5f4253b5be74617c419b003a621aeef959bba33af9c853038494a830bdb7558fc5d16f4e68e365ee5f3a82bef2d843d",
     2156120, CLIENT),

    ("shaderpacks/PixelHaze.zip",
     "https://cdn.modrinth.com/data/Ik0gPrFU/versions/QpRTmmWx/PixelHaze.zip",
     "81d25be82d14efa92730f379cabaaf2708ffaca7",
     "a9d0384533e72e70a983db5fb780b913f60b6e61adf5ec41a26faed4124eb12411d3b924f9eb5842246261b0b96d2c8f31eeb8c88e3d7956367b8077077e000d",
     38530, CLIENT),

    ("shaderpacks/Resident Craft.zip",
     "https://cdn.modrinth.com/data/Ncerk8am/versions/jLtB6l6F/minecraft-shader-template-0.1.0-shaderpack.zip",
     "e5908398efc44edd4929d12473776ed31a19b5ff",
     "57bf78266cff0285dd0e79963800f8fd050aa13c129edf07226103957488548ea6935490af047f643b639f4c3e12469fc96920362941a6f3f8dca98885a52868",
     13043, CLIENT),

    ("shaderpacks/Dread Shader.zip",
     "https://cdn.modrinth.com/data/V4f1kKvr/versions/bJDINNIJ/Dread-Shader.zip",
     "13f6db33d5e3f251635e26d062dd6e4743d4181e",
     "a948b0e0463480317a65c08e780c3b4e5edf855b98e85a39f141b2d6ea4a723ed6f5f20193093cc25b6a9214793303f942696d189f002f885ddbd52fa531a434",
     21002, CLIENT),

    # ------------------------------------------- atmosphere shaders (1.0.2)
    ("shaderpacks/ComplementaryReimagined_r5.10.zip",
     "https://cdn.modrinth.com/data/HVnmMxH1/versions/RKfUB9VC/ComplementaryReimagined_r5.10.zip",
     "ce5f303920c12f858c6b4c27a521fe7a1ba70aea",
     "edced01fb330af215ac73b8d9d31011565089f971f87145b87f413f40ee2128b48970aed26f8e5e879899647ec847b3eefca64bfc013aabbcf25275a207f663d",
     567387, CLIENT),

    ("shaderpacks/ComplementaryUnbound_r5.10.zip",
     "https://cdn.modrinth.com/data/R6NEzAwj/versions/QJiYQisE/ComplementaryUnbound_r5.10.zip",
     "7a9a6b667b89d6b442837c5c367cd6cc8356b400",
     "bccfca6b07ea4c75b3d7c8bfb875de54ecb3ce4416c960194974d887b65c2aebc9ebd2f6911356bdcb2b0f8740ce70adbf2fdf2e39698616c194104400f70e62",
     567391, CLIENT),

    ("shaderpacks/BSL_v10.1.8.zip",
     "https://cdn.modrinth.com/data/Q1vvjJYV/versions/Y68yiql9/BSL_v10.1.8.zip",
     "e89818c8827d0131c102fb480c3c3446624db0df",
     "94a3c25d6820868db624e11347873e6b572e7cd571967d9ac6f4690ca5d37ca302a7645d85eec800e0654541e3825fba14d37b5a25d3996aaf038178552be26f",
     1134153, CLIENT),

    ("shaderpacks/Solas Shader V3.9.zip",
     "https://cdn.modrinth.com/data/EpQFjzrQ/versions/IsqhVpb8/Solas%20Shader%20V3.9.zip",
     "7130b2ef691aa90f117231ba9ab01e7f6fca6fab",
     "754f88b030fc26a6284bc7fdede886af494b227a67e716ce89c8ec5a185f8ad33d0aeac573245c5e39621ac5b24b4778d50ce566647135c2f1b2f63643901c69",
     1243081, CLIENT),

    ("shaderpacks/photon_v1.4.zip",
     "https://cdn.modrinth.com/data/lLqFfGNs/versions/BnGklH5y/photon_v1.4.zip",
     "96c7730f5891a42e39bbf1f0f5a98fa99628a7fe",
     "2dec87084dccf91a72a06235ecbfb3627f066699b5f70583f06077cba61433aedbfa6af28eb68bf1cdc726149cc3d9a587517cfab59e6a24e9d6a40b6874d655",
     3857333, CLIENT),

    ("shaderpacks/Bliss_v2.1.2_(Chocapic13_Shaders_edit).zip",
     "https://cdn.modrinth.com/data/ZvMtQlho/versions/kC2Y8q1P/Bliss_v2.1.2_%28Chocapic13_Shaders_edit%29.zip",
     "ded6c8a98305f8aff90ef781e7585b33b8452cfd",
     "dafc60be4980ec40f40edc0f2625cb0976f3c9ce5ed86383146a120480826bb1de70ef5e38b7f1437294ed4d38c6ef3c82ebef0ae4e00b8cee165788c9c18280",
     1791406, CLIENT),

    ("shaderpacks/Sildur's Enhanced Default v1.20 Fancy.zip",
     "https://cdn.modrinth.com/data/2jvH1Rcl/versions/va1R00mK/Sildur%27s%20Enhanced%20Default%20v1.20%20Fancy.zip",
     "cd57d34cb3dd49fc225a19bc001458c298d17049",
     "f297a1864d6242d1f827c8991d4366d9d9b185871da28d313cef8ca6e10b9eb8c833160f2f634a141e0fa9881738bd57426af41996bedfa6fe7e40e05aec95bc",
     54991, CLIENT),

    ("shaderpacks/MakeUp-UltraFast-9.6a.zip",
     "https://cdn.modrinth.com/data/izsIPI7a/versions/vXV151ey/MakeUp-UltraFast-9.6a.zip",
     "15a581d5aa92cad742c079dd768aae59e0cd379a",
     "4497cf121736a01c9b4b7c3214d96274edaa44d8bfdd65a26b702ecdf2ae1841db6c1f055b66b3b884c38ea6cea3255dbf006f7e66e2976e1423cf1e0a8e23bb",
     406852, CLIENT),
]
