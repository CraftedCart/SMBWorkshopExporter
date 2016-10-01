SMB Workshop Exporter
=====================

Takes a .OBJ file and a config file and exports .LZ.RAW and/ or .LZ

### Usage

To export .LZ.RAW
```sh
java -jar smbworkshopexporter-x.y.jar -m path/to/obj -c path/to/config -o path/to/output/file
```

To export compressed .LZ
```sh
java -jar smbworkshopexporter-x.y.jar -m path/to/obj -c path/to/config -s path/to/output/file
```

You can also use `-h` or `--help` get get a list of available switches