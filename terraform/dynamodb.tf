resource "aws_dynamodb_table" "table" {
  name           = "dynamodb-to-kinesis"
  read_capacity  = 5
  write_capacity = 5
  hash_key       = "Id"

  attribute {
    name = "Id"
    type = "S"
  }
}
