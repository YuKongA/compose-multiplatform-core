"""Generate root Maven module files for all -mingwx64 artifacts in mavenLocal."""
import json, os, re

m2 = os.path.expanduser("~/.m2/repository").replace("\\", "/")
count = 0

for root, dirs, files in os.walk(m2):
    root = root.replace("\\", "/")
    for f in files:
        if not f.endswith(".module") or "-mingwx64-" not in f:
            continue
        m = re.match(r"(.+)-mingwx64-(.+)\.module", f)
        if not m:
            continue
        base_artifact = m.group(1)
        version = m.group(2)
        mingw_artifact = f"{base_artifact}-mingwx64"

        parts = root.split("/")
        m2_idx = next(i for i, p in enumerate(parts) if p == ".m2")
        group_path = "/".join(parts[m2_idx + 2 : -2])
        group_id = group_path.replace("/", ".")

        root_dir = os.path.join(m2, group_path, base_artifact, version)
        root_module = os.path.join(root_dir, f"{base_artifact}-{version}.module")

        if os.path.exists(root_module):
            continue

        with open(os.path.join(root, f)) as mf:
            mingw_data = json.load(mf)

        root_data = {
            "formatVersion": "1.1",
            "component": {"group": group_id, "module": base_artifact, "version": version},
            "variants": [],
        }
        for v in mingw_data.get("variants", []):
            nv = dict(v)
            if "files" in nv:
                for fe in nv["files"]:
                    if "url" in fe:
                        fe["url"] = f"../../{mingw_artifact}/{version}/{fe['url']}"
            root_data["variants"].append(nv)

        os.makedirs(root_dir, exist_ok=True)
        with open(root_module, "w") as mf:
            json.dump(root_data, mf, indent=2)

        pom_path = os.path.join(root_dir, f"{base_artifact}-{version}.pom")
        with open(pom_path, "w") as pf:
            pf.write(
                f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <!-- This module was also published with a richer model, Gradle metadata,  -->
  <!-- which should be used instead. Do not delete the following line which  -->
  <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
  <!-- that they should prefer consuming it instead. -->
  <!-- do_not_remove: published-with-gradle-metadata -->
  <modelVersion>4.0.0</modelVersion>
  <groupId>{group_id}</groupId>
  <artifactId>{base_artifact}</artifactId>
  <version>{version}</version>
  <packaging>pom</packaging>
</project>
"""
            )

        meta_dir = os.path.join(m2, group_path, base_artifact)
        meta_path = os.path.join(meta_dir, "maven-metadata-local.xml")
        if not os.path.exists(meta_path):
            with open(meta_path, "w") as metaf:
                metaf.write(
                    f"""<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>{group_id}</groupId>
  <artifactId>{base_artifact}</artifactId>
  <versioning>
    <latest>{version}</latest>
    <release>{version}</release>
    <versions><version>{version}</version></versions>
  </versioning>
</metadata>
"""
                )

        count += 1
        print(f"OK {group_id}:{base_artifact}:{version}")

print(f"Generated {count} root modules")
