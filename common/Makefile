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
	x86_64-apple-ios \

export NDK_HOME=${current_dir}/cache/ndk/android-ndk-r22b/

all: release
release: android ios
debug: android-debug ios-debug

android: android-release

android-debug: android-install-deps
	cargo ndk -t armeabi-v7a -t arm64-v8a -t x86 -t x86_64 -o ./jniLibs build

android-release: android-install-deps
	cargo ndk -t armeabi-v7a -t arm64-v8a -t x86 -t x86_64 -o ./jniLibs build --release

ios: ios-release

ios-debug: ios-install-deps arti-mobile.h
ios-release: ios-install-deps arti-mobile.h

arti-mobile.h:
	cbindgen src/ios.rs -l c > arti-mobile.h

# Args:
# $1: Target name
# $2: Release mode
define ios-build-rules

ios-$(2)-$(1):
ifeq ($(2), release)
	cargo build --target $(1) --release
else
	cargo build --target $(1)
endif

.PHONY: ios-$(2)-$(1)
ios-$(2): ios-$(2)-$(1)
endef

$(foreach target, $(IOS_TARGETS), $(eval $(call ios-build-rules,$(target),debug)))
$(foreach target, $(IOS_TARGETS), $(eval $(call ios-build-rules,$(target),release)))

clean:
	cargo clean
	-rm -r ./jniLibs

clean-all: clean
	-rm -fr cache/ndk cache/ndk.zip

android-install-deps: cache/ndk cargo-ndk android-targets

ios-install-deps: ios-targets cbindgen

cache/ndk: cache/ndk.zip
	unzip cache/ndk.zip -d $@

cache/ndk.zip:
ifeq ($(OS),Windows_NT)
	wget https://dl.google.com/android/repository/android-ndk-r22b-windows-x86_64.zip -O $@
else ifeq ($(shell uname), Linux)
	wget https://dl.google.com/android/repository/android-ndk-r22b-linux-x86_64.zip -O $@
else ifeq ($(shell uname), Darwin)
	wget https://dl.google.com/android/repository/android-ndk-r22b-darwin-x86_64.zip -O $@
else
	@echo "No ndk found for your system, please download it by yourself"
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
ios-targets:

$(foreach target, $(ANDROID_TARGETS), $(eval $(call target-rules,$(target),android)))
$(foreach target, $(IOS_TARGETS), $(eval $(call target-rules,$(target),ios)))

.PHONY: android-install-deps cargo-ndk android-debug android-release android android-targets
.PHONY: ios-install-deps cbindgen ios-debug ios-release ios ios-targets
.PHONY: all
