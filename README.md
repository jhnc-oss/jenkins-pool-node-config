# Jenkins Pool Node Config

[![ci](https://github.com/jhnc-oss/jenkins-pool-node-config/actions/workflows/ci.yml/badge.svg)](https://github.com/jhnc-oss/jenkins-pool-node-config/actions/workflows/ci.yml)
[![GitHub release](https://img.shields.io/github/release/jhnc-oss/jenkins-pool-node-config.svg)](https://github.com/jhnc-oss/jenkins-pool-node-config/releases)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)
![Java](https://img.shields.io/badge/java-17-green.svg)

Configuration of pool nodes.

## Image types

- **Master**: Image from which the others are created – *builds are not allowed on this type*
- **Test**: Image for testing
- **Prod**: Image used for the actual builds

Each type gets a distinguishable type label, but only *Prod* images get the additional configured labels. 

## Configuration

Configuration is available through *Global Configuration*:

- **Labels**: Labels assigned to *Prod* images (whitespace separated)
- **Master Images**: Name prefix of *Master* images
- **Test Images**: Name prefix of *Test* images
- **Keep Pool Agents offline**: Keep all *Prod* image agents offline
- **Keep specific nodes offline**: Keep all listed agents offline
