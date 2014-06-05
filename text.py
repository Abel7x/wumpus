from z3 import *

z3_x = Int('z3_x')
z3_y = Int('z3_x')



BrisaFunction = Function('BrisaFunction', IntSort(), IntSort(), BoolSort())
PozoFunction = Function('PozoFunction', IntSort(), IntSort(), BoolSort())
HedorFunction = Function('HedorFunction', IntSort(), IntSort(), BoolSort())
WumpusFunction = Function('WumpusFunction', IntSort(), IntSort(), BoolSort())

#Para todo x y que tenga brisa implica que hay un pozo en los adyacentes
pozo = ForAll( [z3_x, z3_y], Implies( BrisaFunction( z3_x, z3_y ), Or( PozoFunction( z3_x + 1, z3_y ), PozoFunction( z3_x - 1, z3_y ), PozoFunction( z3_x, z3_y + 1 ), PozoFunction( z3_x, z3_y - 1 ) ) ) )
r2 = ForAll( [z3_x, z3_y], Implies( Not(BrisaFunction( z3_x, z3_y )), Or( Not(PozoFunction( z3_x + 1, z3_y )), Not(PozoFunction( z3_x - 1, z3_y )), Not(PozoFunction( z3_x, z3_y + 1 )), Not(PozoFunction( z3_x, z3_y - 1 ) ) ) ) )

wumpus = ForAll( [z3_x, z3_y], Implies( HedorFunction( z3_x, z3_y ), Or( WumpusFunction( z3_x + 1, z3_y ), WumpusFunction( z3_x - 1, z3_y ), WumpusFunction( z3_x, z3_y + 1 ), WumpusFunction( z3_x, z3_y - 1 ) ) ) )

reglas = And(pozo, r2, wumpus)

coso = Implies( reglas, And(BrisaFunction(1, 1), HedorFunction(1, 1)))

#prove(coso)

s = Solver()
s.add(coso)
s.check()

var = s.model()

print var