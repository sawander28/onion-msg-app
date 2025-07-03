current_dir := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
rust-installed-targets := $(shell rustup target list --installed)
ANDROID_TARGETS := \
	armv7-linux-androideabi \
	aarch64-linux-android \
	i686-linux-android \
	x86_64-linux-android
FILENAME := libarti_tor.a

export ANDROID_NDK_HOME=${current_dir}cache/ndk/android-ndk-r26b/

# https://stackoverflow.com/questions/75943717/error-building-rust-project-for-android-flutter-arm-linux-androideabi-ranlib
ifeq ($(shell uname), Darwin)
	export TOOLCHAIN=${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/linux-x86_64
	export RANLIB=${TOOLCHAIN}/bin/llvm-ranlib
endif

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

info_guardianproject_arti_ArtiJNI.h:
	javac \
		-h . \
		-classpath ../android/arti/src/main/java/ \
		../android/arti/src/main/java/info/guardianproject/arti/ArtiJNI.java
	find ../android/arti/src/main/java -name "*.class" | xargs rm


# Args:
# $1: OS name
# $2: Rust target name
# $3: Release mode
define apple-build-rules

$(1)-$(3)-$(2): $(1)-install-deps arti-mobile.h
ifeq ($(3), release)
	cargo build --target $(2) --release
else
	cargo build --target $(2)
endif

.PHONY: $(1)-$(3)-$(2)
endef

$(foreach target, $(IOS_TARGETS), $(eval $(call apple-build-rules,ios,$(target),debug)))
$(foreach target, $(IOS_TARGETS), $(eval $(call apple-build-rules,ios,$(target),release)))
$(foreach target, $(MACOS_TARGETS), $(eval $(call apple-build-rules,macos,$(target),debug)))
$(foreach target, $(MACOS_TARGETS), $(eval $(call apple-build-rules,macos,$(target),release)))

clean:
	cargo clean
	cargo uninstall cargo-ndk
	rm -f arti-mobile.h
	-rm -rf ../android/arti/src/main/jniLibs

clean-all: clean
	-rm -rf cache/ndk cache/ndk.zip

android-install-deps: cache/ndk/android-ndk-r26b cargo-ndk android-targets

cache/ndk/android-ndk-r26b: cache/ndk.zip
	bsdunzip cache/ndk.zip -C cache

cache/ndk.zip:
	mkdir -p cache

ifeq ($(OS),Windows_NT)
	wget https://dl.google.com/android/repository/android-ndk-r26b-windows.zip -O $@
else ifeq ($(shell uname), Linux)
	wget https://dl.google.com/android/repository/android-ndk-r26b-linux.zip -O $@
else ifeq ($(shell uname), Darwin)
	wget https://dl.google.com/android/repository/android-ndk-r26b-darwin.zip -O $@
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
