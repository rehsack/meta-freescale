require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot provided by Freescale with focus on QorIQ boards"
PROVIDES += "u-boot"

FILESEXTRAPATHS_append_lx2160acex7 := "${THISDIR}/${PN}-lx2160acex7:"

inherit fsl-u-boot-localversion

LICENSE = "GPLv2 & BSD-3-Clause & BSD-2-Clause & LGPL-2.0 & LGPL-2.1"
LIC_FILES_CHKSUM = " \
    file://Licenses/gpl-2.0.txt;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
    file://Licenses/bsd-2-clause.txt;md5=6a31f076f5773aabd8ff86191ad6fdd5 \
    file://Licenses/bsd-3-clause.txt;md5=4a1190eac56a9db675d58ebe86eaf50c \
    file://Licenses/lgpl-2.0.txt;md5=5f30f0716dfdd0d91eb439ebec522ec2 \
    file://Licenses/lgpl-2.1.txt;md5=4fbd65380cdd255951079008b364516c \
"

SRC_URI = "git://source.codeaurora.org/external/qoriq/qoriq-components/u-boot;nobranch=1 \
    file://0001-patman-Drop-binary-parameter.patch \
    file://0001-patman-Update-command.Run-to-handle-failure-better.patch \
    file://0001-patman-Adjust-command-to-return-strings-instead-of-b.patch \
    file://0001-pylibfdt-Convert-to-Python-3.patch \
    file://0001-binman-Convert-a-few-tests-to-Python-3.patch \
    file://0001-binman-Move-to-use-Python-3.patch \
    file://0001-buildman-Convert-to-Python-3.patch \
"
SRCREV= "1e55b2f9e7f56b76569089b9e950f49c1579580e"

SRC_URI_append_lx2160acex7 = "\
    file://0001-armv8-add-lx2160acex7-build-inclusion.patch
    file://0002-armv8-lx2160acex-misc-hacks-to-get-the-sources-built.patch
    file://0003-armv8-lx2160acex7-defconfig-and-main-platform-includ.patch
    file://0004-armv8-lx2160acex7-common-files-for-platform-support.patch
    file://0005-armv8-lx2160acex7-lx2160acex-device-tree.patch
    file://0006-armv8-lx2160acex7-board-support-files.patch
    file://0007-lx2160a-load-dpl-into-0x80001000-instead-of-0x80d000.patch
    file://0008-uboot-add-nvme-commands-and-for-distroboot.patch
    file://0009-armv8-lx2160acex7-Fix-booting-from-NVMe-drives.patch
    file://0010-nvme-add-accessor-to-namespace-id-and-eui64.patch
    file://0011-nvme-flush-dcache-on-both-r-w-and-the-prp-list.patch
    file://0012-nvme-use-page-aligned-buffer-for-identify-command.patch
    file://0013-lx2160a-cex7-move-from-lsdk-19.06-to-lsdk-19.09.patch
    file://0014-lx2160acex7-pcie-fixup-and-boot-from-eMMC-print.patch
    file://0015-lx2160a-cex7-set-mmc-dev-to-0-when-attempting-sd_boo.patch
    file://0016-lx2160acex7-Misc-fixes-to-support-LSDK-20.04.patch
    file://0017-lx2160acex7-misc-fixes-to-get-booting-from-eMMC-func.patch
"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"
PV_append = "+fslgit"
LOCALVERSION = "+fsl"

INHIBIT_DEFAULT_DEPS = "1"
DEPENDS = "libgcc virtual/${TARGET_PREFIX}gcc bison-native bc-native swig-native python3-native"
DEPENDS_append_qoriq-arm64 = " dtc-native"
DEPENDS_append_qoriq-arm = " dtc-native"
DEPENDS_append_qoriq-ppc = " boot-format-native"

python () {
    if d.getVar("TCMODE") == "external-fsl":
        return

    ml = d.getVar("MULTILIB_VARIANTS")
    arch = d.getVar("OVERRIDES")

    if "e5500-64b:" in arch or "e6500-64b:" in arch:
        if not "lib32" in ml:
            raise bb.parse.SkipPackage("Building the u-boot for this arch requires multilib to be enabled")
        sys_multilib = d.getVar('TARGET_VENDOR') + 'mllib32-linux'
        sys_original = d.getVar('TARGET_VENDOR') + '-' + d.getVar('TARGET_OS')
        workdir = d.getVar('WORKDIR')
        d.setVar('DEPENDS_append', ' lib32-gcc-cross-powerpc lib32-libgcc')
        d.setVar('PATH_append', ':' + d.getVar('STAGING_BINDIR_NATIVE') + '/powerpc' + sys_multilib)
        d.setVar('TOOLCHAIN_OPTIONS', '--sysroot=' + workdir + '/lib32-recipe-sysroot')
        d.setVar("WRAP_TARGET_PREFIX", 'powerpc' + sys_multilib + '-')
    elif "fsl-lsch2-32b:" in arch:
        if not "lib64" in ml:
            raise bb.parse.SkipRecipe("Building the u-boot for this arch requires multilib to be enabled")
        sys_multilib = d.getVar('TARGET_VENDOR') + 'mllib64-linux'
        sys_original = d.getVar('TARGET_VENDOR') + '-' + d.getVar('TARGET_OS')
        workdir = d.getVar('WORKDIR')
        d.setVar('DEPENDS_append', ' lib64-gcc-cross-aarch64 lib64-libgcc')
        d.setVar('PATH_append', ':' + d.getVar('STAGING_BINDIR_NATIVE') + '/aarch64' + sys_multilib)
        d.setVar('TOOLCHAIN_OPTIONS', '--sysroot=' + workdir + '/lib64-recipe-sysroot')
        d.setVar("WRAP_TARGET_PREFIX", 'aarch64' + sys_multilib + '-')
}

LE_UBOOT_FOR_ARMBE_TARGET ?= "0"
ENDIANNESS_GCC = "${@bb.utils.contains("LE_UBOOT_FOR_ARMBE_TARGET", "1", "-mlittle-endian", "", d)}"
ENDIANNESS_LD = "${@bb.utils.contains("LE_UBOOT_FOR_ARMBE_TARGET", "1", "-EL", "", d)}"

WRAP_TARGET_PREFIX ?= "${TARGET_PREFIX}"
EXTRA_OEMAKE = 'CROSS_COMPILE=${WRAP_TARGET_PREFIX} CC="${WRAP_TARGET_PREFIX}gcc ${TOOLCHAIN_OPTIONS} ${ENDIANNESS_GCC}" LD="${WRAP_TARGET_PREFIX}ld ${ENDIANNESS_LD}" V=1'
EXTRA_OEMAKE += 'HOSTCC="${BUILD_CC} ${BUILD_CFLAGS} ${BUILD_LDFLAGS}"'
EXTRA_OEMAKE += 'STAGING_INCDIR=${STAGING_INCDIR_NATIVE} STAGING_LIBDIR=${STAGING_LIBDIR_NATIVE}'

do_compile_append_qoriq() {
    unset i j k
    for config in ${UBOOT_MACHINE}; do
        i=`expr $i + 1`;
        for type in ${UBOOT_CONFIG}; do
            j=`expr $j + 1`;
            for binary in ${UBOOT_BINARIES}; do
                k=`expr $k + 1`
                if [ $j -eq $i ] && [ $k -eq $i ]; then
                    if [ -n "${BOOTFORMAT_CONFIG}" ] && echo "${type}" |grep -q spi;then
                        # regenerate spi binary if BOOTFORMAT_CONFIG is set
                        boot_format ${STAGING_DATADIR_NATIVE}/boot_format/${BOOTFORMAT_CONFIG} \
                            ${config}/u-boot-${type}.${UBOOT_SUFFIX} -spi ${config}/u-boot.format.bin
                        cp ${config}/u-boot.format.bin ${config}/u-boot-${type}.${UBOOT_SUFFIX}
                    elif [ "qspi" = "${type}" ];then
                        cp ${config}/${binary} ${config}/u-boot-${type}-${PV}-${PR}.${UBOOT_SUFFIX}
                    fi
                fi
            done
            unset k
        done
        unset j
    done
    unset i
}


PACKAGES += "${PN}-images"
FILES_${PN}-images += "/boot"
COMPATIBLE_MACHINE = "(qoriq)"
