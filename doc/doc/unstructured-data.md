<!--
Copyright 2023 ZETARIS Pty Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies
or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
-->

## Processing unstructured data
Along with GenAI, data engineer need to prepare data to transform data into more suitable representation.
Especially for the unstructured data(text, image, video, pdf, etc), data engineer handle them in different way.
For the flexibility and standard way of accessing or processing unstructured data, Lightning catalog support processing unstructured in the form of SQL.

This document articulates how to ingest unstructured data and run SQL over them

## Support datasource v2 in Spark
Apache spark made in significant improvement with data source v2 in terms of performance and scan optimization.
https://docs.google.com/document/d/1DDXCTCrup4bKWByTalkXWgavcPdvur8a4eEu8x1BzPM/edit#heading=h.nxp4e06em2bl

Lightning catalog implemented datasource v2 for all type of unstructured data

## support different scan types
Unstructured data are saved into any file system including blob storage along with directory hierarchy.
In that regard, Lightning supports 3 different scan types

### file scan
this mode simply scans only files under the given folder

### recursive scan
this mode recursively scans all files under the given folder. 
It virtually adds subdir column to the schema that show relative directory from the given root directory 

### partition scan
this mode scan partition directory formed "column=value", and it adds partition column to the schema.

## PDF

## Ingest File Source
```bash
-- make sure parent namespace is created
DROP NAMESPACE IF EXISTS lightning.datasource.file
CREATE NAMESPACE lightning.datasource.file

-- file_scan will scan leaf files under the root bucket : s3a://doctor/prescriptions
REGISTER OR REPLACE PDF DATASOURCE prescriptions OPTIONS (
  path "s3a://doctor/prescriptions",
  scanType "file_scan", --optional 'file_scan' by default
  pathGlobFilter "*.pdf", -- optional, '*' by default
  "fs.s3a.access.key" "AKIAUDI43XXXXXXXXXXX",
  "fs.s3a.secret.key" "xfGoSe+mmgXXXXXXXXXXXXX"
) NAMESPACE lightning.datasource.file

DESCRIBE TABLE lightning.datasource.file.prescriptions

will display the below schema:
+-----------+---------+-------+
|   col_name|data_type|comment|
+-----------+---------+-------+
|       type|   string|   NULL|
|       path|   string|   NULL|
| modifiedat|timestamp|   NULL|
|sizeinbytes|   bigint|   NULL|
|    preview|   string|   NULL|
+-----------+---------+-------+

SELECT * FROM lightning.datasource.file.prescriptions
will display:

+----+--------------------+-------------------+-----------+--------------------+
|type|                path|         modifiedat|sizeinbytes|             preview|
+----+--------------------+-------------------+-----------+--------------------+
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       9482|Aa \nBb \nCc \ndd...|
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       9384|Ee \nFf \nGg \ngg...|
+----+--------------------+-------------------+-----------+--------------------+
```

### recursive scan mode
```bash
-- Ingest the below files and sub directories
Lprescriptions
  Laa_88.pdf
  Lee_12.pdf
  Lsubdir
    Lpdf-subdir.pdf
    
REGISTER OR REPLACE PDF DATASOURCE prescriptions OPTIONS (
  path "s3a://doctor/prescriptions",
  scanType "recursive_scan", 
  pathGlobFilter "*.pdf", 
  "fs.s3a.access.key" "AKIAUDI43XXXXXXXXXXX",
  "fs.s3a.secret.key" "xfGoSe+mmgXXXXXXXXXXXXX"
) NAMESPACE lightning.datasource.file

SELECT * FROM lightning.datasource.file.prescriptions

will display the below with "subir" column
+----+--------------------+-------------------+-----------+--------------------+-------+
|type|                path|         modifiedat|sizeinbytes|             preview| subdir|
+----+--------------------+-------------------+-----------+--------------------+-------+
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       9482|Aa \nBb \nCc \ndd...|       |
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       9384|Ee \nFf \nGg \ngg...|       |
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       8786|Aa \nBb \nCc \nCc...|/subdir|
+----+--------------------+-------------------+-----------+--------------------+-------+
```

### part scan mode
```bash
-- Ingest the below partition directory
Lprescriptions
  Lct=alpha
    Laabbccdd.pdf
    Leeffgghh.pdf
  Lct=alphanumeric
    Laa_88.pdf
    Lee_12.pdf
  Lct=numeric
    L11223344.pdf
    L55667788.pdf
    
REGISTER OR REPLACE PDF DATASOURCE prescriptions OPTIONS (
  path "s3a://doctor/prescriptions",
  scanType "parts_scan", 
  pathGlobFilter "*.pdf", 
  "fs.s3a.access.key" "AKIAUDI43XXXXXXXXXXX",
  "fs.s3a.secret.key" "xfGoSe+mmgXXXXXXXXXXXXX"
) NAMESPACE lightning.datasource.file

SELECT * FROM lightning.datasource.file.prescriptions

will display the below with "ct" column
+----+--------------------+-------------------+-----------+--------------------+------------+
|type|                path|         modifiedat|sizeinbytes|             preview|          ct|
+----+--------------------+-------------------+-----------+--------------------+------------+
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       8786|Aa \nBb \nCc \nCc...|       alpha|
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       8666|ee \n# \nhh \ngg ...|       alpha|
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       9482|Aa \nBb \nCc \ndd...|alphanumeric|
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       9384|Ee \nFf \nGg \ngg...|alphanumeric|
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       8416|11 \n22 \n33 \n44...|     numeric|
| pdf|file:///Users/jae...|2024-06-24 15:54:33|       8507|55 \n66 \n77 \n88 \n|     numeric|
+----+--------------------+-------------------+-----------+--------------------+------------+
```

### ingest text file
```bash
-- Ingest the below partition directory
Lprescriptions
  Lct=alpha
    aa.txt
    bb.txt
  Lct=alphanumeric
    11aa.txt
    22bb.txt
  Lct=numeric
    11.txt
    22.txt
    
REGISTER OR REPLACE TEXT DATASOURCE prescriptions OPTIONS (
  path "s3a://doctor/prescriptions",
  scanType "parts_scan", 
  pathGlobFilter "*.txt", 
  "fs.s3a.access.key" "AKIAUDI43XXXXXXXXXXX",
  "fs.s3a.secret.key" "xfGoSe+mmgXXXXXXXXXXXXX"
) NAMESPACE lightning.datasource.file

SELECT * FROM lightning.datasource.file.prescriptions

will display the below with "ct" column
+----+--------------------+-------------------+-----------+-------+------------+
|type|                path|         modifiedat|sizeinbytes|preview|          ct|
+----+--------------------+-------------------+-----------+-------+------------+
| txt|file:///Users/jae...|2024-06-26 09:52:15|          3|   aa\n|       alpha|
| txt|file:///Users/jae...|2024-06-26 09:52:15|          3|   bb\n|       alpha|
| txt|file:///Users/jae...|2024-06-26 09:52:15|          5| 11aa\n|alphanumeric|
| txt|file:///Users/jae...|2024-06-26 09:52:15|          5| 22bb\n|alphanumeric|
| txt|file:///Users/jae...|2024-06-26 09:52:15|          3|   11\n|     numeric|
| txt|file:///Users/jae...|2024-06-26 09:52:15|          3|   22\n|     numeric|
+----+--------------------+-------------------+-----------+-------+------------+

SELECT * FROM lightning.datasource.file.prescriptions.content

will display:

+--------------------+-----------+------------+
|                path|textcontent|          ct|
+--------------------+-----------+------------+
|file:///Users/jae...|       11\n|     numeric|
|file:///Users/jae...|       22\n|     numeric|
|file:///Users/jae...|     11aa\n|alphanumeric|
|file:///Users/jae...|     22bb\n|alphanumeric|
|file:///Users/jae...|       aa\n|       alpha|
|file:///Users/jae...|       bb\n|       alpha|
+--------------------+-----------+------------+
```

### ingest image files
```bash
REGISTER OR REPLACE IMAGE DATASOURCE xray_scan OPTIONS (
  path "s3a://doctor/xray-scan",
  scanType "file_scan", 
  pathGlobFilter "{*.png,*.jpg}", 
  "fs.s3a.access.key" "AKIAUDI43XXXXXXXXXXX",
  "fs.s3a.secret.key" "xfGoSe+mmgXXXXXXXXXXXXX"
) NAMESPACE lightning.datasource.file

-- default thumbnail resolution 100 x 1000

SELECT * FROM lightning.datasource.file.xray_scan

will display :
+----+--------------------+-------------------+-----------+-----+------+--------------------+
|type|                path|         modifiedat|sizeinbytes|width|height|      imagethumbnail|
+----+--------------------+-------------------+-----------+-----+------+--------------------+
| jpg|file:///Users/jae...|2024-06-26 09:52:15|       4909|  230|   148|[89 50 4E 47 0D 0...|
| png|file:///Users/jae...|2024-06-26 09:52:15|       6131|  270|   148|[89 50 4E 47 0D 0...|
+----+--------------------+-------------------+-----------+-----+------+--------------------+

-- with custom thumbnail resolution, 50 x 50
REGISTER OR REPLACE IMAGE DATASOURCE xray_scan OPTIONS (
  path "s3a://doctor/xray-scan",
  scanType "file_scan", 
  pathGlobFilter "{*.png,*.jpg}", 
  image_thumbnail_with "50",
  image_thumbnail_height "50",
  "fs.s3a.access.key" "AKIAUDI43XXXXXXXXXXX",
  "fs.s3a.secret.key" "xfGoSe+mmgXXXXXXXXXXXXX"
) NAMESPACE lightning.datasource.file
  
SELECT * FROM FROM lightning.datasource.file.xray_scan

will display with thumbnail resolution, 50 x 32:

+----+--------------------+-------------------+-----------+-----+------+--------------------+
|type|                path|         modifiedat|sizeinbytes|width|height|      imagethumbnail|
+----+--------------------+-------------------+-----------+-----+------+--------------------+
| jpg|file:///Users/jae...|2024-06-26 09:52:15|       4909|  230|   148|[89 50 4E 47 0D 0...|
| png|file:///Users/jae...|2024-06-26 09:52:15|       6131|  270|   148|[89 50 4E 47 0D 0...|
+----+--------------------+-------------------+-----------+-----+------+--------------------+

SELECT * FROM FROM lightning.datasource.file.xray_scan.content

will display actual content:
+--------------------+--------------------+
|                path|          bincontent|
+--------------------+--------------------+
|file:///Users/jae...|[89 50 4E 47 0D 0...|
|file:///Users/jae...|[89 50 4E 47 0D 0...|
+--------------------+--------------------+
```
