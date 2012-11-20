function        (state, x, y) {
  for (var i = 0; i < state.map.length; i++) {
    var entity = state.map[i];
    
    if (entity.x == x && entity.y == y) {
      entity.contract_start = java.lang.System.currentTimeMillis();
    }
  }

  return {state:state, result:'ok'};
}
