import request from '@/utils/request'

// 添加区块链记录
export function addRecord(recordId, description, remark) {
  return request({
    url: '/fisco/record/add',
    method: 'post',
    params: {
      recordId,
      description,
      remark
    }
  })
}

// 批量添加区块链记录
export function addRecordBatch(data) {
  return request({
    url: '/fisco/record/addBatch',
    method: 'post',
    data: data
  })
}

// 获取区块链记录
export function getRecord(recordId) {
  return request({
    url: '/fisco/record/get/' + recordId,
    method: 'get'
  })
}

// 检查记录是否存在
export function checkRecordExists(recordId) {
  return request({
    url: '/fisco/record/exists/' + recordId,
    method: 'get'
  })
}