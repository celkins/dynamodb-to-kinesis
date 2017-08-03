resource "aws_kinesis_stream" "stream" {
  name        = "dynamodb-to-kinesis"
  shard_count = 1
}
