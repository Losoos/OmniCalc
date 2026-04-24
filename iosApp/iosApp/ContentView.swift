import SwiftUI
import shared

struct ContentView: View {
    @State private var input: String = ""
    @State private var result: String = "0"

    var body: some View {
        VStack(spacing: 20) {
            Text("OmniCalc iOS")
                .font(.largeTitle)
                .padding()

            TextField("Zadejte příklad", text: $input)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
                .keyboardType(.decimalPad)

            Button("Vypočítat") {
                if let solved = SharedSolver.shared.solve(input: input) {
                    result = String(format: "%.2f", solved)
                } else {
                    result = "Chyba"
                }
            }
            .padding()
            .background(Color.blue)
            .foregroundColor(.white)
            .cornerRadius(10)

            Text("Výsledek: \(result)")
                .font(.title)
                .padding()
        }
    }
}
