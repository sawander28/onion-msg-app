current_dir := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
rust-installed-targets := $(shell rustup target list --installed)
ANDROID_TARGETS := \
	armv7-linux-androideabi \
	aarch64-linux-android \
	i686-linux-android \
	x86_64-linux-android
FILENAME := libarti_messenger.a

export ANDROID_NDK_HOME=${HOME}/android/ndk/android-ndk-r26d
export TOOLCHAIN=${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/linux-x86_64
export RANLIB=${TOOLCHAIN}/bin/llvm-ranlib


all: release
release: android
debug: android-debug

android: android-release

android-debug: android-install-deps
	cargo ndk -t armeabi-v7a -t arm64-v8a -t x86 -t x86_64 -o ../android/arti/src/main/jniLibs build

android-debug-x86: android-install-deps
	cargo ndk -t x86 -o ../android/arti/src/main/jniLibs build

android-debug-x86_64: android-install-deps
	cargo ndk -t x86_64 -o ../android/arti/src/main/jniLibs build

android-release: android-install-deps
	RUSTFLAGS='-C link-arg=-s' cargo ndk -t armeabi-v7a -t arm64-v8a -t x86 -t x86_64 -o ../android/arti/src/main/jniLibs build --release

android-release-arm64: android-install-deps
	RUSTFLAGS='-C link-arg=-s' cargo ndk -t arm64-v8a -o ../android/arti/src/main/jniLibs build --release

android-release-x86_64: android-install-deps
	RUSTFLAGS='-C link-arg=-s' cargo ndk -t x86_64 -o ../android/arti/src/main/jniLibs build --release

wandsas_arti_ArtiJNI.h:
	javac \
		-h . \
		-classpath ../android/arti/src/main/java/ \
		../android/arti/src/main/java/wandsas/arti/ArtiJNI.java
	find ../android/arti/src/main/java -name "*.class" | xargs rm


clean:
	cargo clean
	cargo uninstall cargo-ndk
	rm -f arti-mobile.h
	-rm -rf ../android/arti/src/main/jniLibs

clean-all: clean
	-rm -rf cache/ndk cache/ndk.zip

#android-install-deps: android-ndk-r26d cargo-ndk android-targets
android-install-deps: cargo-ndk android-targets

android-ndk-r26d: cache/ndk.zip
	bsdunzip cache/ndk.zip -C cache

cache/ndk.zip:
	mkdir -p cache

ifeq ($(OS),Windows_NT)
	wget https://dl.google.com/android/repository/android-ndk-r26d-windows.zip -O $@
else ifeq ($(shell uname), Linux)
	wget https://dl.google.com/android/repository/android-ndk-r26d-linux.zip -O $@
else ifeq ($(shell uname), Darwin)
	wget https://dl.google.com/android/repository/android-ndk-r26d-darwin.zip -O $@
else
	@echo "No NDK found for your system, please download it by yourself."
	@false
endif

cargo-ndk:
ifeq ($(shell command -v cargo-ndk),)
	cargo install cargo-ndk
endif

# Args:
# $1: Rust target name
# $2: OS name
define target-rules

$(2)-target-$(1):
ifeq ($$(filter $(1), $$(rust-installed-targets)),)
	rustup target install $(1)
endif
.PHONY: $(2)-target-$(1)
$(2)-targets: $(2)-target-$(1)

endef

android-targets:
$(foreach target, $(ANDROID_TARGETS), $(eval $(call target-rules,$(target),android)))


.PHONY: android-install-deps cargo-ndk android-debug android-release android android-targets
.PHONY: all
