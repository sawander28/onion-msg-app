current_dir := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
rust-installed-targets := $(shell rustup target list --installed)
ANDROID_TARGETS := \
	armv7-linux-androideabi \
	aarch64-linux-android \
	i686-linux-android \
	x86_64-linux-android
IOS_TARGETS := \
	aarch64-apple-ios \
	aarch64-apple-ios-sim \
	x86_64-apple-ios
MACOS_TARGETS := \
	aarch64-apple-darwin \
	x86_64-apple-darwin

export NDK_HOME=${current_dir}/cache/ndk/android-ndk-r22b/

all: release
release: android ios macos
debug: android-debug ios-debug macos-debug

android: android-release

android-debug: android-install-deps info_guardianproject_arti_ArtiJNI.h
	cargo ndk -t armeabi-v7a -t arm64-v8a -t x86 -t x86_64 -o ./jniLibs build

android-release: android-install-deps info_guardianproject_arti_ArtiJNI.h
	RUSTFLAGS='-C link-arg=-s' cargo ndk -t armeabi-v7a -t arm64-v8a -t x86 -t x86_64 -o ./jniLibs build --release

android-release-arm64: android-install-deps info_guardianproject_arti_ArtiJNI.h
	RUSTFLAGS='-C link-arg=-s' cargo ndk -t arm64-v8a -o ./jniLibs build --release

android-release-x86_64: android-install-deps info_guardianproject_arti_ArtiJNI.h
	RUSTFLAGS='-C link-arg=-s' cargo ndk -t x86_64 -o ./jniLibs build --release

info_guardianproject_arti_ArtiJNI.h:
	javac -h . ../android/arti/src/main/java/info/guardianproject/arti/ArtiJNI.java
	rm ../android/arti/src/main/java/info/guardianproject/arti/ArtiJNI.class

ios: ios-release

ios-debug: ios-install-deps arti-mobile.h
ios-release: ios-install-deps arti-mobile.h

macos: macos-release

macos-debug: macos-install-deps arti-mobile.h
macos-release: macos-install-deps arti-mobile.h

arti-mobile.h:
	cbindgen src/apple.rs -l c > arti-mobile.h

# Args:
# $1: OS name
# $2: Target name
# $3: Release mode
define apple-build-rules

$(1)-$(3)-$(2):
ifeq ($(3), release)
	cargo build --target $(2) --release
else
	cargo build --target $(2)
endif

.PHONY: $(1)-$(3)-$(2)
$(1)-$(3): $(1)-$(3)-$(2)
endef

$(foreach target, $(IOS_TARGETS), $(eval $(call apple-build-rules,ios,$(target),debug)))
$(foreach target, $(IOS_TARGETS), $(eval $(call apple-build-rules,ios,$(target),release)))
$(foreach target, $(MACOS_TARGETS), $(eval $(call apple-build-rules,macos,$(target),debug)))
$(foreach target, $(MACOS_TARGETS), $(eval $(call apple-build-rules,macos,$(target),release)))

clean:
	cargo clean
	-rm -r ./jniLibs

clean-all: clean
	-rm -fr cache/ndk cache/ndk.zip

android-install-deps: cache/ndk cargo-ndk android-targets

ios-install-deps: ios-targets cbindgen

macos-install-deps: macos-targets cbindgen

cache/ndk/android-ndk-r22b/README.md: cache/ndk.zip
	unzip cache/ndk.zip -d $@
	touch cache/ndk.zip #MacOS' unzip might delete the zip file, so recreate an empty file to calm make.

cache/ndk.zip:
	mkdir -p cache

ifeq ($(OS),Windows_NT)
	wget https://dl.google.com/android/repository/android-ndk-r22b-windows-x86_64.zip -O $@
else ifeq ($(shell uname), Linux)
	wget https://dl.google.com/android/repository/android-ndk-r22b-linux-x86_64.zip -O $@
else ifeq ($(shell uname), Darwin)
	wget https://dl.google.com/android/repository/android-ndk-r22b-darwin-x86_64.zip -O $@
else
	@echo "No NDK found for your system, please download it by yourself."
	@false
endif

cargo-ndk:
ifeq ($(shell command -v cargo-ndk),)
	cargo install cargo-ndk
endif

cbindgen:
ifeq ($(shell command -v cbindgen),)
	cargo install cbindgen
endif

# Args:
# $1: Target name
# $2: Target operating system
define target-rules

$(2)-target-$(1):
ifeq ($$(findstring $(1), $$(rust-installed-targets)),)
	rustup target install $(1)
endif
.PHONY: $(2)-target-$(1)
$(2)-targets: $(2)-target-$(1)

endef

android-targets:
$(foreach target, $(ANDROID_TARGETS), $(eval $(call target-rules,$(target),android)))

ios-targets:
$(foreach target, $(IOS_TARGETS), $(eval $(call target-rules,$(target),ios)))

macos-targets:
$(foreach target, $(MACOS_TARGETS), $(eval $(call target-rules,$(target),macos)))

.PHONY: android-install-deps cargo-ndk android-debug android-release android android-targets
.PHONY: ios-install-deps cbindgen ios-debug ios-release ios ios-targets
.PHONY: macos-install-deps macos-debug macos-release macos macos-targets
.PHONY: all
