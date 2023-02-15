Vagrant.configure("2") do |config|
  config.vm.provider :libvirt do |libvirt|
    libvirt.memory = 4096
    libvirt.cpus = 4
  end
  config.vm.provider "virtualbox" do |v|
    v.memory = 4096
    v.cpus = 4
  end
  config.vm.box = "debian/bullseye64"
  config.vm.provision "shell", inline: <<-SHELL
    set -xeuo pipefail

    apt update -yqq
    apt upgrade -yqq
    apt install -yqq build-essential curl openjdk-11-jdk-headless wget unzip
    sudo -iu vagrant -- bash -c 'curl --proto "=https" --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y'
  SHELL
end
