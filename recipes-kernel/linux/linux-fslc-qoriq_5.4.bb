# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Mainline/LTS rebased NXP/QorIQ patches + FSLC patches."
DESCRIPTION = "Linux kernel based on LTS kernel used by FSL Community BSP in order to \
provide support for some backported features and fixes, or because it was applied in linux-next \
and takes some time to become part of a stable version, or because it is not applicable for \
upstreaming."

require recipes-kernel/linux/linux-qoriq.inc

FILESEXTRAPATHS_append_lx2160acex7 := "${THISDIR}/${PN}-lx2160acex7:"

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

LINUX_VERSION = "5.4.43"

SRCBRANCH = "5.4.y+qoriq+fslc"
SRCREV = "e464752884a9a134daf72fe8babb2236df4fdcff"
SRC_URI := "git://github.com/Freescale/linux-fslc.git;branch=${SRCBRANCH}"

SRC_URI_append_lx2160acex7 = "\
    file://0001-arm64-dts-lx2160a-add-lx2160acex7-device-tree-build.patch \
    file://0002-arm64-dts-lx2160a-add-lx2160acex7-device-tree.patch \
    file://0005-arm64-dts-lx2160a-cex7-add-ltc3882-support.patch \
    file://0006-arm64-dts-lx2160a-cex7-add-on-module-eeproms.patch \
    file://0007-pci-hotplug-declare-IDT-bridge-as-hotpluggabl-bridge.patch \
    file://0008-pci-spr2803-quirk-to-fix-class-ID.patch \
"
