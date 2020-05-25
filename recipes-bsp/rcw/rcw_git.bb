SUMMARY = "Reset Configuration Word"
DESCRIPTION = "Reset Configuration Word - hardware boot-time parameters for the QorIQ targets"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=44a0d0fad189770cc022af4ac6262cbe"

DEPENDS += "tcl-native"

inherit deploy siteinfo

SRC_URI = "git://source.codeaurora.org/external/qoriq/qoriq-components/rcw;nobranch=1"
SRCREV = "e0fab6d9b61003caef577f7474c2fac61e6ba2ff"

SRC_URI_append_lx2160acex7 = "\
	file://0001-lx2160acex7-misc-RCW-files.patch \
	file://0002-Set-io-pads-as-GPIO.patch \
	file://0003-S2-enable-gen3-xspi-increase-divisor-to-28.patch \
	file://0004-refactor-a009531-a008851-and-a011270.patch \
	file://0006-lx2160a-add-SVR-check-for-a050234-to-apply-only-on-r.patch \
	file://0007-lx2160acex7-pcie-workarounds-and-fan-full-speed.patch \
	file://0008-lx2160a-add-generic-bootloc-section.patch \
	file://0009-lx2160acex7-remove-all-predefined-RCW-files.patch \
"

S = "${WORKDIR}/git"

export PYTHON = "${USRBINPATH}/python3"

M="${@d.getVar('MACHINE').replace('-64b','').replace('-32b','').replace('-${SITEINFO_ENDIANNESS}','')}"

BOARD_TARGETS="${M}"
BOARD_TARGETS_ls2088ardb="${M} ${M}_rev1.1"
BOARD_TARGETS_ls1088ardb-pb="ls1088ardb"
BOARD_TARGETS_lx2160ardb = "${M} ${M}_rev2"

do_configure_prepend_lx2160acex7 () {
    for BT in ${BOARD_TARGETS}
    do
	mkdir -p ${S}/${BOARD_TARGETS}/${SERDES}
	cat <<EOF >${S}/${BOARD_TARGETS}/README
The RCW directories for lx2160acex7 are created based on existing SERDES
configuration. Currently created automatically - later maybe by building
cross product of serdes & ddr speeds in a final commit.
EOF
	cat <<EOF >${S}/${BOARD_TARGETS}/${SERDES}/${SPEED}.rcw
#include <configs/lx2160a_defaults.rcwi>
#include <configs/lx2160a_${SPEED}.rcwi>
#include <configs/lx2160a_${SERDES}.rcwi>
EOF
    done
}

do_compile () {
    oe_runmake BOARDS="${BOARD_TARGETS}" DESTDIR=${D}/boot/rcw/
}

do_install () {
    oe_runmake BOARDS="${BOARD_TARGETS}" DESTDIR=${D}/boot/rcw/ install
}

do_deploy () {
    install -d ${DEPLOYDIR}/rcw
    cp -r ${D}/boot/rcw/* ${DEPLOYDIR}/rcw/
}
addtask deploy after do_install

PACKAGES += "${PN}-image"
FILES_${PN}-image += "/boot"

COMPATIBLE_MACHINE = "(qoriq)"
PACKAGE_ARCH = "${MACHINE_ARCH}"
