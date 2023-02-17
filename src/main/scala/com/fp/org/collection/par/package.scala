package scala.collection

// Copyright (C) Igor Skuratov. All rights reserved.
// Licensed under the MIT License. See LICENSE in project root for information.

package object par extends par.ParDefs {

  object Configuration {
   val manualOptimizations = sys.props.get("scala.collection.par.range.manual_optimizations").map(_.toBoolean).getOrElse(true)
  }


}

