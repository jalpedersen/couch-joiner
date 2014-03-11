function(doc) {
  if (doc.deviceId && doc.user) {
    emit(doc.user, doc.deviceId);
  }
}
