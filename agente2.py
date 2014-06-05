# Proyecto Final Inteligencia Artificial
# Mundo del Wumpus
# Diana Angelica Gamez Diaz
# Ricardo Nales Amato


from z3 import *

# Inicia declaracion de funciones #

P = Function('P', IntSort(), IntSort(), BoolSort()) # Funcion para localizar hoyo
B = Function('B', IntSort(), IntSort(), BoolSort()) # Funcion para localizar brisa
W = Function('W', IntSort(), IntSort(), BoolSort())	# Funcion para localizar wumpus
H = Function('H', IntSort(), IntSort(), BoolSort()) # Funcion para localizar hedor
R = Function('S', IntSort(), IntSort(), BoolSort()) # Funcion para localizar brillo
O = Function('O', IntSort(), IntSort(), BoolSort()) # Funcion para localizar oro
S = Function('S', IntSort(), IntSort(), BoolSort()) # Funcion para casilla segura

# Termina declaracion de funciones #


# Inicia declaracion de variables #	

x = Int('x') # Variable de posicion en eje x
y = Int('y') # Variable de posicion en eje y

# Termina declaracion de variables #


# Inicia sentencia de hoyos y brisas #

Brisa = ForAll([x,y],Implies(B(x,y), And(P(x+1,y), P(x-1,y), P(x,y+1), P(x,y-1))))
Hoyo = Implies(B(x,y), Or(P(x+1,y), P(x-1,y), P(x,y+1), P(x,y-1))) # Existe hoyo en alguna casilla adyacente a brisa
# NoBrisa = Implies(Not(P(x,y)), And(Not(B(x+1,y)), Not(B(x-1,y)), Not(B(x,y+1)), Not(B(x,y-1)))) # No existe brisa ni hoyo

# Hoyo = ForAll([x,y],P(x,y) == And(B(x+1,y), B(x-1,y), B(x,y+1), B(x,y-1))) # Existe hoyo en alguna casilla adyacente a brisa
# Brisa = ForAll([x,y],B(x,y))

# Termina sentencia de hoyos y brisas #


# Inicia sentencia de hedor y wumpus #

Hedor = ForAll([x,y],Implies(H(x,y), And(W(x+1,y), W(x-1,y), W(x,y+1), W(x,y-1)))) # Existe wumpus en alguna casilla adyacente a hedor
Wumpus = ForAll([x,y],W(x,y) == And(H(x+1,y), H(x-1,y), H(x,y+1), H(x,y-1))) # Existe hedor en casillas adyacentes a wumpus
NoWumpus = ForAll([x,y],Not(W(x,y)) == And(Not(H(x+1,y)), Not(H(x-1,y)), Not(H(x,y+1)), Not(H(x,y-1))))

# Termina sentencia de hedor y wumpus #

# Inicia sentencia de casilla segura #
Segura = ForAll([x,y],S(x,y) == And(Not(H(x,y)),Not(W(x,y)),Not(B(x,y)),Not(P(x,y))))

# Inicia sentencia de oro y brillo #

Oro = And(Implies(R(x,y), O(x,y)),Implies(O(x,y),R(x,y)))
Brillo = Implies(O(x,y), R(x,y))

# Termina sentencia de oro y brillo #
Segura = ForAll([x,y],S(x,y) == And(Not(H(x,y)),Not(B(x,y))))
SeguraHedor = ForAll([x,y],S(x,y) == Or(Not(H(x+1,y)), Not(H(x-1,y)), Not(H(x,y+1)), Not(H(x,y-1))))

# Inicia conocimiento #

#conocimiento = And( Not(H(1,1)),Not(W(1,1)),Not(B(1,1)),Not(P(1,1)) )
conocimiento = And(H(1,2))
# Termina conocimiento #

# Probar en base a conocimiento #

print( prove( Implies(And(SeguraHedor,conocimiento),S(1,1)) ) )


print conocimiento;

