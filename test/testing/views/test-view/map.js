function(doc) {
  if (doc.deviceId) {
    emit([doc.user, doc.deviceId], doc._id);
  }
} 
